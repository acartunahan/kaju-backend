package tr.depo.backend.firma;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.depo.backend.common.exception.IsKuraliException;
import tr.depo.backend.common.paging.SayfaCevabi;
import tr.depo.backend.common.paging.SiralamaDogrulayici;
import tr.depo.backend.common.persistence.ReferansYukleyici;
import tr.depo.backend.common.service.KullanimReferansi;
import tr.depo.backend.common.service.SilmeAltyapisi;
import tr.depo.backend.common.service.SoftDeleteServisSablonu;
import tr.depo.backend.firma.dto.CreateFirmaRequest;
import tr.depo.backend.firma.dto.FirmaResponse;
import tr.depo.backend.firma.dto.UpdateFirmaRequest;
import tr.depo.domain.tanim.Depo;
import tr.depo.domain.tanim.Firma;
import tr.depo.domain.tanim.Ihale;
import tr.depo.domain.tanim.MalzemeTuru;

@Service
@Transactional(readOnly = true)
public class FirmaServiceImpl extends SoftDeleteServisSablonu<Firma> implements FirmaService {

    static final String KAYNAK = "Firma";
    static final String DEPO_ALANI = "depoId";
    static final String DEMIRBAS_ZORUNLU = "firma.demirbas-zorunlu";
    static final String SARF_AD_ZORUNLU = "firma.sarf-ad-zorunlu";
    static final Set<String> SIRALAMA_ALANLARI = Set.of("id", "tur", "ad", "kayitTarihi", "olusturmaZamani");

    private static final List<KullanimReferansi> KULLANIM_REFERANSLARI = List.of(
        new KullanimReferansi(Ihale.class, "firma", "ihale"));

    private final FirmaRepository repository;
    private final FirmaMapper mapper;
    private final ReferansYukleyici referansYukleyici;

    public FirmaServiceImpl(FirmaRepository repository, FirmaMapper mapper, ReferansYukleyici referansYukleyici,
                            SilmeAltyapisi silmeAltyapisi) {
        super(repository, silmeAltyapisi, KAYNAK);
        this.repository = repository;
        this.mapper = mapper;
        this.referansYukleyici = referansYukleyici;
    }

    @Override
    public FirmaResponse getir(Long id) {
        return mapper.cevap(aktifBul(id));
    }

    @Override
    public SayfaCevabi<FirmaResponse> listele(Pageable sayfa) {
        SiralamaDogrulayici.dogrula(sayfa, SIRALAMA_ALANLARI);
        return SayfaCevabi.of(repository.findAll(sayfa).map(mapper::cevap));
    }

    @Override
    @Transactional
    public FirmaResponse olustur(CreateFirmaRequest istek) {
        turunZorunluAlanlari(istek.tur(), istek.ad(), istek.durumKodu(), istek.kayitTarihi());
        Firma firma = mapper.yeni(istek);
        firma.setDepo(referansYukleyici.aktif(Depo.class, istek.depoId(), DEPO_ALANI));
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
        firma.setDepo(referansYukleyici.aktif(Depo.class, istek.depoId(), DEPO_ALANI));
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
