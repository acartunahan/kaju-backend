package tr.depo.backend.sayim.satir;

import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.depo.backend.common.paging.SayfaCevabi;
import tr.depo.backend.common.paging.SiralamaDogrulayici;
import tr.depo.backend.common.persistence.ReferansYukleyici;
import tr.depo.backend.common.service.AltKayitServisSablonu;
import tr.depo.backend.common.service.SilmeAltyapisi;
import tr.depo.backend.sayim.SayimRepository;
import tr.depo.backend.sayim.satir.dto.CreateSayimSatiriRequest;
import tr.depo.backend.sayim.satir.dto.SayimSatiriResponse;
import tr.depo.backend.sayim.satir.dto.UpdateSayimSatiriRequest;
import tr.depo.domain.envanter.Envanter;
import tr.depo.domain.sayim.Sayim;
import tr.depo.domain.sayim.SayimSatiri;
import tr.depo.domain.tanim.Depo;

@Service
@Transactional(readOnly = true)
public class SayimSatiriServiceImpl extends AltKayitServisSablonu<SayimSatiri, Sayim> implements SayimSatiriService {

    static final String KAYNAK = "Sayım satırı";
    static final String UST_KAYNAK = "Sayım";
    static final Set<String> SIRALAMA_ALANLARI = Set.of("id", "islemTarihi", "olusturmaZamani");

    private final SayimSatiriRepository repository;
    private final SayimSatiriMapper mapper;
    private final ReferansYukleyici referansYukleyici;

    public SayimSatiriServiceImpl(SayimSatiriRepository repository, SayimSatiriMapper mapper,
                                  SayimRepository sayimRepository, ReferansYukleyici referansYukleyici,
                                  SilmeAltyapisi silmeAltyapisi) {
        super(repository, sayimRepository, silmeAltyapisi, KAYNAK, UST_KAYNAK);
        this.repository = repository;
        this.mapper = mapper;
        this.referansYukleyici = referansYukleyici;
    }

    @Override
    public SayimSatiriResponse getir(Long sayimId, Long id) {
        return mapper.cevap(aktifAltKayit(sayimId, id));
    }

    @Override
    public SayfaCevabi<SayimSatiriResponse> listele(Long sayimId, Pageable sayfa) {
        SiralamaDogrulayici.dogrula(sayfa, SIRALAMA_ALANLARI);
        aktifUst(sayimId);
        return SayfaCevabi.of(repository.findBySayim_Id(sayimId, sayfa).map(mapper::cevap));
    }

    @Override
    @Transactional
    public SayimSatiriResponse olustur(Long sayimId, CreateSayimSatiriRequest istek) {
        Sayim sayim = aktifUst(sayimId);
        SayimSatiriKurallari.kapsamaUygunOlmali(istek.tur(), sayim.getKapsam());
        SayimSatiriKurallari.dogrula(istek.tur(), istek.islem(), istek.islemTarihi(), istek.malzemeTuru(),
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
        SayimSatiriKurallari.dogrula(satir.getTur(), istek.islem(), istek.islemTarihi(), istek.malzemeTuru(),
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
        Envanter envanter = referansYukleyici.aktif(Envanter.class, envanterId, "envanterId");
        Depo depo = referansYukleyici.aktif(Depo.class, depoId, "depoId");
        satir.setEnvanter(envanter);
        satir.setDepo(depo);
    }
}
