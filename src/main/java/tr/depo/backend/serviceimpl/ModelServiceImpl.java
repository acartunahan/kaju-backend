package tr.depo.backend.serviceimpl;

import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.depo.backend.dto.CreateModelRequest;
import tr.depo.backend.dto.KullanimReferansi;
import tr.depo.backend.dto.ModelResponse;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.UpdateModelRequest;
import tr.depo.backend.exception.IsKuraliException;
import tr.depo.backend.mapper.ModelMapper;
import tr.depo.backend.repository.ModelRepository;
import tr.depo.backend.service.ModelService;
import tr.depo.backend.service.ReferansService;
import tr.depo.backend.service.SilmeService;
import tr.depo.backend.util.SiralamaDogrulayici;
import tr.depo.domain.envanter.Envanter;
import tr.depo.domain.tanim.Depo;
import tr.depo.domain.tanim.Kategori;
import tr.depo.domain.tanim.MalzemeTuru;
import tr.depo.domain.tanim.Marka;
import tr.depo.domain.tanim.Model;

@Service
@Transactional(readOnly = true)
public class ModelServiceImpl extends AbstractSoftDeleteServiceImpl<Model> implements ModelService {

    static final String KAYNAK = "Model";
    static final String MARKA_ALANI = "markaId";
    static final String KATEGORI_ALANI = "kategoriId";
    static final String DEPO_ALANI = "depoId";
    static final String SARF_ZORUNLU = "model.sarf-zorunlu";
    static final String MARKA_BOSALTILAMAZ = "model.marka-bosaltilamaz";
    static final Set<String> SIRALAMA_ALANLARI = Set.of("id", "tur", "ad", "kayitTarihi", "olusturmaZamani");

    private static final List<KullanimReferansi> KULLANIM_REFERANSLARI = List.of(
        new KullanimReferansi(Envanter.class, "model", "envanter"));

    private final ModelRepository repository;
    private final ModelMapper mapper;
    private final ReferansService referansService;

    public ModelServiceImpl(ModelRepository repository, ModelMapper mapper, ReferansService referansService,
                            SilmeService silmeService) {
        super(repository, silmeService, KAYNAK);
        this.repository = repository;
        this.mapper = mapper;
        this.referansService = referansService;
    }

    @Override
    public ModelResponse getir(Long id) {
        return mapper.cevap(aktifBul(id));
    }

    @Override
    public SayfaResponse<ModelResponse> listele(Pageable sayfa) {
        SiralamaDogrulayici.dogrula(sayfa, SIRALAMA_ALANLARI);
        return SayfaResponse.of(repository.findAll(sayfa).map(mapper::cevap));
    }

    @Override
    @Transactional
    public ModelResponse olustur(CreateModelRequest istek) {
        Marka marka = marka(istek.markaId(), istek.tur());
        Model model = mapper.yeni(istek, marka);
        model.setMarka(marka);
        model.setKategori(kategori(istek.kategoriId(), istek.tur()));
        model.setDepo(referansService.aktif(Depo.class, istek.depoId(), DEPO_ALANI));
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
        markaBosaltilmamali(model, istek.markaId());
        Marka marka = marka(istek.markaId(), model.getTur());
        Kategori kategori = kategori(istek.kategoriId(), model.getTur());
        mapper.guncelle(istek, model);
        model.setMarka(marka);
        model.setKategori(kategori);
        model.setDepo(referansService.aktif(Depo.class, istek.depoId(), DEPO_ALANI));
        repository.flush();
        return mapper.cevap(model);
    }

    @Override
    protected List<KullanimReferansi> kullanimReferanslari() {
        return KULLANIM_REFERANSLARI;
    }

    private Marka marka(Long markaId, MalzemeTuru tur) {
        return referansService.aktifVeAyniTurde(Marka.class, markaId, MARKA_ALANI, tur, Marka::getTur);
    }

    private Kategori kategori(Long kategoriId, MalzemeTuru tur) {
        return referansService.aktifVeAyniTurde(Kategori.class, kategoriId, KATEGORI_ALANI, tur, Kategori::getTur);
    }

    /**
     * İş kuralı: yeni modelde marka zorunlu (istek doğrulaması); güncellemede dolu (silinmemiş) marka boşaltılamaz
     * (422). Legacy'den markasız gelen demirbaş modeli markasız kalabilir ve yine güncellenebilir.
     */
    private static void markaBosaltilmamali(Model model, Long markaId) {
        Marka mevcut = model.getMarka();
        if (markaId == null && mevcut != null && !mevcut.isSilindi()) {
            throw new IsKuraliException(MARKA_BOSALTILAMAZ, "Modelin markası boşaltılamaz");
        }
    }

    /** Domain kuralı (CK_MODEL_SARF): PUT tam güncellemedir; sarf modelinde marka ve aktif boşaltılamaz (422). */
    private static void sarfZorunluAlanlari(MalzemeTuru tur, Long markaId, Boolean aktif) {
        if (tur == MalzemeTuru.SARF && (markaId == null || aktif == null)) {
            throw new IsKuraliException(SARF_ZORUNLU, "Sarf modelinde markaId ve aktif zorunlu");
        }
    }
}
