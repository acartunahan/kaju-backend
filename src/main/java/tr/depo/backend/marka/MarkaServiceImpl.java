package tr.depo.backend.marka;

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
import tr.depo.backend.marka.dto.CreateMarkaRequest;
import tr.depo.backend.marka.dto.MarkaResponse;
import tr.depo.backend.marka.dto.UpdateMarkaRequest;
import tr.depo.domain.envanter.Envanter;
import tr.depo.domain.tanim.Depo;
import tr.depo.domain.tanim.MalzemeTuru;
import tr.depo.domain.tanim.Marka;
import tr.depo.domain.tanim.Model;

@Service
@Transactional(readOnly = true)
public class MarkaServiceImpl extends SoftDeleteServisSablonu<Marka> implements MarkaService {

    static final String KAYNAK = "Marka";
    static final String DEPO_ALANI = "depoId";
    static final String SARF_AKTIF_ZORUNLU = "marka.sarf-aktif-zorunlu";
    static final Set<String> SIRALAMA_ALANLARI = Set.of("id", "tur", "ad", "kayitTarihi", "olusturmaZamani");

    private static final List<KullanimReferansi> KULLANIM_REFERANSLARI = List.of(
        new KullanimReferansi(Model.class, "marka", "model"),
        new KullanimReferansi(Envanter.class, "marka", "envanter"));

    private final MarkaRepository repository;
    private final MarkaMapper mapper;
    private final ReferansYukleyici referansYukleyici;

    public MarkaServiceImpl(MarkaRepository repository, MarkaMapper mapper, ReferansYukleyici referansYukleyici,
                            SilmeAltyapisi silmeAltyapisi) {
        super(repository, silmeAltyapisi, KAYNAK);
        this.repository = repository;
        this.mapper = mapper;
        this.referansYukleyici = referansYukleyici;
    }

    @Override
    public MarkaResponse getir(Long id) {
        return mapper.cevap(aktifBul(id));
    }

    @Override
    public SayfaCevabi<MarkaResponse> listele(Pageable sayfa) {
        SiralamaDogrulayici.dogrula(sayfa, SIRALAMA_ALANLARI);
        return SayfaCevabi.of(repository.findAll(sayfa).map(mapper::cevap));
    }

    @Override
    @Transactional
    public MarkaResponse olustur(CreateMarkaRequest istek) {
        Marka marka = mapper.yeni(istek);
        marka.setDepo(referansYukleyici.aktif(Depo.class, istek.depoId(), DEPO_ALANI));
        Marka kaydedilen = repository.save(marka);
        repository.flush();
        return mapper.cevap(kaydedilen);
    }

    @Override
    @Transactional
    public MarkaResponse guncelle(Long id, UpdateMarkaRequest istek) {
        Marka marka = aktifBul(id);
        surumuDogrula(marka, istek.versiyon());
        sarfAktifZorunlu(marka.getTur(), istek.aktif());
        mapper.guncelle(istek, marka);
        marka.setDepo(referansYukleyici.aktif(Depo.class, istek.depoId(), DEPO_ALANI));
        repository.flush();
        return mapper.cevap(marka);
    }

    @Override
    protected List<KullanimReferansi> kullanimReferanslari() {
        return KULLANIM_REFERANSLARI;
    }

    /** PUT tam güncellemedir; sarf markasında aktif bilgisi boşaltılamaz (domain ve CK_MARKA_SARF_AKTIF). */
    private static void sarfAktifZorunlu(MalzemeTuru tur, Boolean aktif) {
        if (tur == MalzemeTuru.SARF && aktif == null) {
            throw new IsKuraliException(SARF_AKTIF_ZORUNLU, "Sarf markasında aktif zorunlu");
        }
    }
}
