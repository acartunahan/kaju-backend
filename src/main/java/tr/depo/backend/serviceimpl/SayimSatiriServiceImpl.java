package tr.depo.backend.serviceimpl;

import java.time.LocalDateTime;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.depo.backend.dto.CreateSayimSatiriRequest;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.SayimSatiriResponse;
import tr.depo.backend.dto.UpdateSayimSatiriRequest;
import tr.depo.backend.exception.IsKuraliException;
import tr.depo.backend.mapper.SayimSatiriMapper;
import tr.depo.backend.repository.SayimRepository;
import tr.depo.backend.repository.SayimSatiriRepository;
import tr.depo.backend.service.ReferansService;
import tr.depo.backend.service.SayimSatiriService;
import tr.depo.backend.service.SilmeService;
import tr.depo.backend.util.SiralamaDogrulayici;
import tr.depo.domain.envanter.Envanter;
import tr.depo.domain.sayim.Sayim;
import tr.depo.domain.sayim.SayimIslemi;
import tr.depo.domain.sayim.SayimKapsami;
import tr.depo.domain.sayim.SayimSatiri;
import tr.depo.domain.sayim.YerelSayimTuru;
import tr.depo.domain.tanim.Depo;

@Service
@Transactional(readOnly = true)
public class SayimSatiriServiceImpl extends AbstractAltKayitServiceImpl<SayimSatiri, Sayim> implements SayimSatiriService {

    static final String KAYNAK = "Sayım satırı";
    static final String UST_KAYNAK = "Sayım";
    static final Set<String> SIRALAMA_ALANLARI = Set.of("id", "islemTarihi", "olusturmaZamani");

    // Satır kuralları (SayimSatiri entity'si ve CK_SAYIM_SATIRI_* kısıtları): entity bunları istisnayla zorlar,
    // burada önceden kontrol edilerek anlamlı 422 döner.
    static final String ISLEM_TURE_UYGUN_DEGIL = "sayim-satiri.islem-tur";
    static final String YEREL_ZORUNLU = "sayim-satiri.yerel-zorunlu";
    static final String GENEL_TARIH_ZORUNLU = "sayim-satiri.genel-tarih-zorunlu";
    static final String ENVANTER_YALNIZ_ZIMMET = "sayim-satiri.envanter-yalniz-zimmet";
    static final String KAPSAM_UYUMSUZ = "sayim-satiri.kapsam-uyumsuz";

    private final SayimSatiriRepository repository;
    private final SayimSatiriMapper mapper;
    private final ReferansService referansService;

    public SayimSatiriServiceImpl(SayimSatiriRepository repository, SayimSatiriMapper mapper,
                                  SayimRepository sayimRepository, ReferansService referansService,
                                  SilmeService silmeService) {
        super(repository, sayimRepository, silmeService, KAYNAK, UST_KAYNAK);
        this.repository = repository;
        this.mapper = mapper;
        this.referansService = referansService;
    }

    @Override
    public SayimSatiriResponse getir(Long sayimId, Long id) {
        return mapper.cevap(aktifAltKayit(sayimId, id));
    }

    @Override
    public SayfaResponse<SayimSatiriResponse> listele(Long sayimId, Pageable sayfa) {
        SiralamaDogrulayici.dogrula(sayfa, SIRALAMA_ALANLARI);
        aktifUst(sayimId);
        return SayfaResponse.of(repository.findBySayim_Id(sayimId, sayfa).map(mapper::cevap));
    }

    @Override
    @Transactional
    public SayimSatiriResponse olustur(Long sayimId, CreateSayimSatiriRequest istek) {
        Sayim sayim = aktifUst(sayimId);
        kapsamaUygunOlmali(istek.tur(), sayim.getKapsam());
        dogrula(istek.tur(), istek.islem(), istek.islemTarihi(), istek.malzemeTuru(),
            istek.malzemeEskiRef(), istek.zimmetEskiRef(), istek.envanterId());
        SayimSatiri satir = mapper.yeni(istek);
        satir.setSayim(sayim);
        iliskileriBagla(satir, istek.envanterId(), istek.depoId());
        SayimSatiri kaydedilen = repository.save(satir);
        repository.flush();
        return mapper.cevap(kaydedilen);
    }

    @Override
    @Transactional
    public SayimSatiriResponse guncelle(Long sayimId, Long id, UpdateSayimSatiriRequest istek) {
        SayimSatiri satir = aktifAltKayit(sayimId, id);
        surumuDogrula(satir, istek.versiyon());
        dogrula(satir.getTur(), istek.islem(), istek.islemTarihi(), istek.malzemeTuru(),
            istek.malzemeEskiRef(), istek.zimmetEskiRef(), istek.envanterId());
        // Malzeme türü ZIMMET'ten değişebilir; domain envanterli satırda bunu reddettiği için önce bağlantı kaldırılır.
        satir.setEnvanter(null);
        mapper.guncelle(istek, satir);
        iliskileriBagla(satir, istek.envanterId(), istek.depoId());
        repository.flush();
        return mapper.cevap(satir);
    }

    @Override
    protected Sayim ustKaydi(SayimSatiri satir) {
        return satir.getSayim();
    }

    private void iliskileriBagla(SayimSatiri satir, Long envanterId, Long depoId) {
        Envanter envanter = referansService.aktif(Envanter.class, envanterId, "envanterId");
        Depo depo = referansService.aktif(Depo.class, depoId, "depoId");
        satir.setEnvanter(envanter);
        satir.setDepo(depo);
    }

    /**
     * Yeni satırın türü sayımın kapsamıyla aynı olmalı (yerel sayıma yerel satır). Yalnızca yeni satırda uygulanır:
     * domain bu tutarlılığı legacy veri yüzünden zorlamaz (HANDOFF 5.16) ve satırın türü sonradan değişmez.
     */
    static void kapsamaUygunOlmali(SayimKapsami satirTuru, SayimKapsami sayimKapsami) {
        if (satirTuru != sayimKapsami) {
            throw new IsKuraliException(KAPSAM_UYUMSUZ, satirTuru + " satırı " + sayimKapsami + " sayımına eklenemez",
                satirTuru, sayimKapsami);
        }
    }

    static void dogrula(SayimKapsami tur, SayimIslemi islem, LocalDateTime islemTarihi, YerelSayimTuru malzemeTuru,
                        String malzemeEskiRef, String zimmetEskiRef, Long envanterId) {
        if (!islem.gecerliMi(tur)) {
            throw new IsKuraliException(ISLEM_TURE_UYGUN_DEGIL, islem + " işlemi " + tur + " satırında geçersiz", islem, tur);
        }
        if (tur == SayimKapsami.YEREL) {
            yerelKurallari(malzemeTuru, malzemeEskiRef, zimmetEskiRef, envanterId);
        } else if (islemTarihi == null) {
            throw new IsKuraliException(GENEL_TARIH_ZORUNLU, "Genel sayım satırında islemTarihi zorunlu");
        }
    }

    private static void yerelKurallari(YerelSayimTuru malzemeTuru, String malzemeEskiRef, String zimmetEskiRef,
                                       Long envanterId) {
        if (malzemeTuru == null || bos(malzemeEskiRef) || bos(zimmetEskiRef)) {
            throw new IsKuraliException(YEREL_ZORUNLU, "Yerel sayım satırında malzemeTuru, malzemeEskiRef, zimmetEskiRef zorunlu");
        }
        if (envanterId != null && malzemeTuru != YerelSayimTuru.ZIMMET) {
            throw new IsKuraliException(ENVANTER_YALNIZ_ZIMMET, "Yerel sayımda envantere yalnızca ZIMMET satırı bağlanır");
        }
    }

    private static boolean bos(String deger) {
        return deger == null || deger.isBlank();
    }
}
