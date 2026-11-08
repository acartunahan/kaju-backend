package tr.depo.backend.serviceimpl;

import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.depo.backend.dto.CreateEnvanterHareketiRequest;
import tr.depo.backend.dto.EnvanterHareketiResponse;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.exception.IsKuraliException;
import tr.depo.backend.mapper.EnvanterHareketiMapper;
import tr.depo.backend.repository.EnvanterHareketiRepository;
import tr.depo.backend.service.EnvanterHareketiService;
import tr.depo.backend.service.EnvanterService;
import tr.depo.backend.service.ReferansService;
import tr.depo.backend.util.AktifKayitUtil;
import tr.depo.backend.util.SiralamaDogrulayici;
import tr.depo.domain.envanter.Envanter;
import tr.depo.domain.envanter.EnvanterHareketi;
import tr.depo.domain.tanim.Depo;
import tr.depo.domain.tanim.MalzemeTuru;

@Service
@Transactional(readOnly = true)
public class EnvanterHareketiServiceImpl implements EnvanterHareketiService {

    static final String KAYNAK = "Envanter hareketi";
    static final String DEMIRBAS_HAREKET_NO_ZORUNLU = "hareket.demirbas-hareket-no-zorunlu";
    static final Set<String> SIRALAMA_ALANLARI = Set.of("id", "kayitTarihi", "olusturmaZamani");

    private static final Sort EN_YENI_ONCE = Sort.by(Sort.Direction.DESC, "kayitTarihi", "id");

    private final EnvanterHareketiRepository repository;
    private final EnvanterHareketiMapper mapper;
    private final ReferansService referansService;
    private final EnvanterService envanterService;

    public EnvanterHareketiServiceImpl(EnvanterHareketiRepository repository, EnvanterHareketiMapper mapper,
                                       ReferansService referansService, EnvanterService envanterService) {
        this.repository = repository;
        this.mapper = mapper;
        this.referansService = referansService;
        this.envanterService = envanterService;
    }

    @Override
    public EnvanterHareketiResponse getir(Long id) {
        return mapper.cevap(AktifKayitUtil.bul(repository, id, KAYNAK));
    }

    @Override
    public SayfaResponse<EnvanterHareketiResponse> listele(Long depoId, Pageable sayfa) {
        SiralamaDogrulayici.dogrula(sayfa, SIRALAMA_ALANLARI);
        return SayfaResponse.of((depoId == null ? repository.findAll(sayfa) : repository.deponunHareketleri(depoId, sayfa))
            .map(mapper::cevap));
    }

    @Override
    public SayfaResponse<EnvanterHareketiResponse> demirbasHareketleri(Long demirbasId, Pageable sayfa) {
        return envanterHareketleri(demirbasId, MalzemeTuru.DEMIRBAS, "Demirbaş", sayfa);
    }

    @Override
    public SayfaResponse<EnvanterHareketiResponse> sarfHareketleri(Long sarfMalzemesiId, Pageable sayfa) {
        return envanterHareketleri(sarfMalzemesiId, MalzemeTuru.SARF, "Sarf malzemesi", sayfa);
    }

    private SayfaResponse<EnvanterHareketiResponse> envanterHareketleri(Long envanterId, MalzemeTuru tur, String kaynakAdi,
                                                                       Pageable sayfa) {
        envanterService.aktifBul(envanterId, tur, kaynakAdi);
        SiralamaDogrulayici.dogrula(sayfa, SIRALAMA_ALANLARI);
        return SayfaResponse.of(repository.findByEnvanter_Id(envanterId,
            SiralamaDogrulayici.varsayilanSiralama(sayfa, EN_YENI_ONCE)).map(mapper::cevap));
    }

    @Override
    @Transactional
    public EnvanterHareketiResponse olustur(CreateEnvanterHareketiRequest istek) {
        demirbasHareketNoZorunlu(istek);
        Envanter envanter = referansService.aktifVeAyniTurde(Envanter.class, istek.envanterId(), "envanterId",
            istek.tur(), Envanter::getTur);
        Depo neredenDepo = referansService.aktif(Depo.class, istek.neredenDepoId(), "neredenDepoId");
        Depo nereyeDepo = referansService.aktif(Depo.class, istek.nereyeDepoId(), "nereyeDepoId");
        Depo alanDepo = referansService.aktif(Depo.class, istek.alanDepoId(), "alanDepoId");

        EnvanterHareketi hareket = mapper.yeni(istek);
        hareket.setEnvanter(envanter);
        hareket.setNeredenDepo(neredenDepo);
        hareket.setNereyeDepo(nereyeDepo);
        hareket.setAlanDepo(alanDepo);
        EnvanterHareketi kaydedilen = repository.save(hareket);
        repository.flush();
        return mapper.cevap(kaydedilen);
    }

    /** Domain kuralı (CK_ENV_HAREKETI_DEMIRBAS): eksikse entity'ye gitmeden 422. */
    private static void demirbasHareketNoZorunlu(CreateEnvanterHareketiRequest istek) {
        if (istek.tur() == MalzemeTuru.DEMIRBAS && istek.hareketNoKodu() == null) {
            throw new IsKuraliException(DEMIRBAS_HAREKET_NO_ZORUNLU, "Demirbaş hareketinde hareketNoKodu zorunlu");
        }
    }
}
