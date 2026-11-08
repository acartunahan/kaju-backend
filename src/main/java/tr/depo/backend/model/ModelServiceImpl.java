package tr.depo.backend.model;

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
import tr.depo.backend.model.dto.CreateModelRequest;
import tr.depo.backend.model.dto.ModelResponse;
import tr.depo.backend.model.dto.UpdateModelRequest;
import tr.depo.domain.envanter.Envanter;
import tr.depo.domain.tanim.Depo;
import tr.depo.domain.tanim.Kategori;
import tr.depo.domain.tanim.MalzemeTuru;
import tr.depo.domain.tanim.Marka;
import tr.depo.domain.tanim.Model;

@Service
@Transactional(readOnly = true)
public class ModelServiceImpl extends SoftDeleteServisSablonu<Model> implements ModelService {

    static final String KAYNAK = "Model";
    static final String MARKA_ALANI = "markaId";
    static final String KATEGORI_ALANI = "kategoriId";
    static final String DEPO_ALANI = "depoId";
    static final String SARF_ZORUNLU = "model.sarf-zorunlu";
    static final String SARF_MARKA_ZORUNLU = "model.sarf-marka-zorunlu";
    static final Set<String> SIRALAMA_ALANLARI = Set.of("id", "tur", "ad", "kayitTarihi", "olusturmaZamani");

    private static final List<KullanimReferansi> KULLANIM_REFERANSLARI = List.of(
        new KullanimReferansi(Envanter.class, "model", "envanter"));

    private final ModelRepository repository;
    private final ModelMapper mapper;
    private final ReferansYukleyici referansYukleyici;

    public ModelServiceImpl(ModelRepository repository, ModelMapper mapper, ReferansYukleyici referansYukleyici,
                            SilmeAltyapisi silmeAltyapisi) {
        super(repository, silmeAltyapisi, KAYNAK);
        this.repository = repository;
        this.mapper = mapper;
        this.referansYukleyici = referansYukleyici;
    }

    @Override
    public ModelResponse getir(Long id) {
        return mapper.cevap(aktifBul(id));
    }

    @Override
    public SayfaCevabi<ModelResponse> listele(Pageable sayfa) {
        SiralamaDogrulayici.dogrula(sayfa, SIRALAMA_ALANLARI);
        return SayfaCevabi.of(repository.findAll(sayfa).map(mapper::cevap));
    }

    @Override
    @Transactional
    public ModelResponse olustur(CreateModelRequest istek) {
        sarfMarkaZorunlu(istek.tur(), istek.markaId());
        Marka marka = marka(istek.markaId(), istek.tur());
        Model model = mapper.yeni(istek, marka);
        model.setMarka(marka);
        model.setKategori(kategori(istek.kategoriId(), istek.tur()));
        model.setDepo(referansYukleyici.aktif(Depo.class, istek.depoId(), DEPO_ALANI));
        Model kaydedilen = repository.save(model);
        repository.flush();
        return mapper.cevap(kaydedilen);
    }

    @Override
    @Transactional
    public ModelResponse guncelle(Long id, UpdateModelRequest istek) {
        Model model = aktifBul(id);
        surumuDogrula(model, istek.versiyon());
        sarfZorunluAlanlari(model.getTur(), istek.markaId(), istek.aktif());
        Marka marka = marka(istek.markaId(), model.getTur());
        Kategori kategori = kategori(istek.kategoriId(), model.getTur());
        mapper.guncelle(istek, model);
        model.setMarka(marka);
        model.setKategori(kategori);
        model.setDepo(referansYukleyici.aktif(Depo.class, istek.depoId(), DEPO_ALANI));
        repository.flush();
        return mapper.cevap(model);
    }

    @Override
    protected List<KullanimReferansi> kullanimReferanslari() {
        return KULLANIM_REFERANSLARI;
    }

    private Marka marka(Long markaId, MalzemeTuru tur) {
        return referansYukleyici.aktifVeAyniTurde(Marka.class, markaId, MARKA_ALANI, tur, Marka::getTur);
    }

    private Kategori kategori(Long kategoriId, MalzemeTuru tur) {
        return referansYukleyici.aktifVeAyniTurde(Kategori.class, kategoriId, KATEGORI_ALANI, tur, Kategori::getTur);
    }

    /** Yeni sarf modelinde marka zorunlu; aktif boşsa {@code true} olur (mapper, Marka ile aynı davranış). */
    private static void sarfMarkaZorunlu(MalzemeTuru tur, Long markaId) {
        if (tur == MalzemeTuru.SARF && markaId == null) {
            throw new IsKuraliException(SARF_MARKA_ZORUNLU, "Sarf modelinde markaId zorunlu");
        }
    }

    /** Domain kuralı (CK_MODEL_SARF): PUT tam güncellemedir; sarf modelinde marka ve aktif boşaltılamaz (422). */
    private static void sarfZorunluAlanlari(MalzemeTuru tur, Long markaId, Boolean aktif) {
        if (tur == MalzemeTuru.SARF && (markaId == null || aktif == null)) {
            throw new IsKuraliException(SARF_ZORUNLU, "Sarf modelinde markaId ve aktif zorunlu");
        }
    }
}
