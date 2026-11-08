package tr.depo.backend.serviceimpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.depo.backend.dto.CreateFirmaRequest;
import tr.depo.backend.dto.FirmaResponse;
import tr.depo.backend.dto.KullanimReferansi;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.TurFiltreRequest;
import tr.depo.backend.dto.UpdateFirmaRequest;
import tr.depo.backend.exception.IsKuraliException;
import tr.depo.backend.mapper.FirmaMapper;
import tr.depo.backend.repository.FirmaRepository;
import tr.depo.backend.service.FirmaService;
import tr.depo.backend.service.ReferansService;
import tr.depo.backend.service.SilmeService;
import tr.depo.backend.util.SiralamaDogrulayici;
import tr.depo.domain.tanim.Depo;
import tr.depo.domain.tanim.Firma;
import tr.depo.domain.tanim.Ihale;
import tr.depo.domain.tanim.MalzemeTuru;

@Service
@Transactional(readOnly = true)
public class FirmaServiceImpl extends AbstractSoftDeleteServiceImpl<Firma> implements FirmaService {

    static final String KAYNAK = "Firma";
    static final String DEPO_ALANI = "depoId";
    static final String DEMIRBAS_ZORUNLU = "firma.demirbas-zorunlu";
    static final String SARF_AD_ZORUNLU = "firma.sarf-ad-zorunlu";
    static final Set<String> SIRALAMA_ALANLARI = Set.of("id", "tur", "ad", "kayitTarihi", "olusturmaZamani");

    private static final List<KullanimReferansi> KULLANIM_REFERANSLARI = List.of(
        new KullanimReferansi(Ihale.class, "firma", "ihale"));

    private final FirmaRepository repository;
    private final FirmaMapper mapper;
    private final ReferansService referansService;

    public FirmaServiceImpl(FirmaRepository repository, FirmaMapper mapper, ReferansService referansService,
                            SilmeService silmeService) {
        super(repository, silmeService, KAYNAK);
        this.repository = repository;
        this.mapper = mapper;
        this.referansService = referansService;
    }

    @Override
    public FirmaResponse getir(Long id) {
        return mapper.cevap(aktifBul(id));
    }

    @Override
    public SayfaResponse<FirmaResponse> listele(TurFiltreRequest filtre, Pageable sayfa) {
        SiralamaDogrulayici.dogrula(sayfa, SIRALAMA_ALANLARI);
        return SayfaResponse.of(repository.filtrele(filtre.tur(), sayfa).map(mapper::cevap));
    }

    @Override
    @Transactional
    public FirmaResponse olustur(CreateFirmaRequest istek) {
        turunZorunluAlanlari(istek.tur(), istek.ad(), istek.durumKodu(), istek.kayitTarihi());
        Firma firma = mapper.yeni(istek);
        firma.setDepo(referansService.aktif(Depo.class, istek.depoId(), DEPO_ALANI));
        Firma kaydedilen = repository.save(firma);
        repository.flush();
        return mapper.cevap(kaydedilen);
    }

    @Override
    @Transactional
    public FirmaResponse guncelle(Long id, UpdateFirmaRequest istek) {
        Firma firma = aktifBul(id);
        surumuDogrula(firma, istek.versiyon());
        turunZorunluAlanlari(firma.getTur(), istek.ad(), istek.durumKodu(), istek.kayitTarihi());
        mapper.guncelle(istek, firma);
        firma.setDepo(referansService.aktif(Depo.class, istek.depoId(), DEPO_ALANI));
        repository.flush();
        return mapper.cevap(firma);
    }

    @Override
    protected List<KullanimReferansi> kullanimReferanslari() {
        return KULLANIM_REFERANSLARI;
    }

    /** Domain kuralı (CK_FIRMA_DEMIRBAS, CK_FIRMA_SARF): eksikse entity'ye gitmeden 422. */
    private static void turunZorunluAlanlari(MalzemeTuru tur, String ad, String durumKodu, LocalDateTime kayitTarihi) {
        if (tur == MalzemeTuru.DEMIRBAS && (durumKodu == null || kayitTarihi == null)) {
            throw new IsKuraliException(DEMIRBAS_ZORUNLU, "Demirbaş firmasında durumKodu ve kayitTarihi zorunlu");
        }
        if (tur == MalzemeTuru.SARF && (ad == null || ad.isBlank())) {
            throw new IsKuraliException(SARF_AD_ZORUNLU, "Sarf firmasında ad zorunlu");
        }
    }
}
