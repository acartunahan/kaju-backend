package tr.depo.backend.serviceimpl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.depo.backend.dto.CreateKategoriRequest;
import tr.depo.backend.dto.KategoriResponse;
import tr.depo.backend.dto.KullanimReferansi;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.UpdateKategoriRequest;
import tr.depo.backend.exception.IsKuraliException;
import tr.depo.backend.mapper.KategoriMapper;
import tr.depo.backend.repository.KategoriRepository;
import tr.depo.backend.service.KategoriService;
import tr.depo.backend.service.ReferansService;
import tr.depo.backend.service.SilmeService;
import tr.depo.backend.util.SiralamaDogrulayici;
import tr.depo.domain.envanter.Envanter;
import tr.depo.domain.tanim.Depo;
import tr.depo.domain.tanim.Kategori;
import tr.depo.domain.tanim.MalzemeTuru;
import tr.depo.domain.tanim.Model;

@Service
@Transactional(readOnly = true)
public class KategoriServiceImpl extends AbstractSoftDeleteServiceImpl<Kategori> implements KategoriService {

    static final String KAYNAK = "Kategori";
    static final String UST_ALANI = "ustKategoriId";
    static final String DEPO_ALANI = "depoId";
    static final String DEMIRBAS_DURUM_KODU_ZORUNLU = "kategori.demirbas-durum-kodu-zorunlu";
    static final String DONGU = "kategori.dongu";
    static final Set<String> SIRALAMA_ALANLARI = Set.of("id", "tur", "ad", "kayitTarihi", "olusturmaZamani");

    private static final List<KullanimReferansi> KULLANIM_REFERANSLARI = List.of(
        new KullanimReferansi(Kategori.class, "ust", "alt kategori"),
        new KullanimReferansi(Model.class, "kategori", "model"),
        new KullanimReferansi(Envanter.class, "kategori", "envanter"));

    private final KategoriRepository repository;
    private final KategoriMapper mapper;
    private final ReferansService referansService;

    public KategoriServiceImpl(KategoriRepository repository, KategoriMapper mapper,
                               ReferansService referansService, SilmeService silmeService) {
        super(repository, silmeService, KAYNAK);
        this.repository = repository;
        this.mapper = mapper;
        this.referansService = referansService;
    }

    @Override
    public KategoriResponse getir(Long id) {
        return mapper.cevap(aktifBul(id));
    }

    @Override
    public SayfaResponse<KategoriResponse> listele(Pageable sayfa) {
        SiralamaDogrulayici.dogrula(sayfa, SIRALAMA_ALANLARI);
        return SayfaResponse.of(repository.findAll(sayfa).map(mapper::cevap));
    }

    @Override
    @Transactional
    public KategoriResponse olustur(CreateKategoriRequest istek) {
        demirbasDurumKoduZorunlu(istek.tur(), istek.durumKodu());
        Kategori kategori = mapper.yeni(istek);
        kategori.setUst(ustKategori(istek.ustKategoriId(), istek.tur()));
        kategori.setDepo(referansService.aktif(Depo.class, istek.depoId(), DEPO_ALANI));
        Kategori kaydedilen = repository.save(kategori);
        repository.flush();
        return mapper.cevap(kaydedilen);
    }

    @Override
    @Transactional
    public KategoriResponse guncelle(Long id, UpdateKategoriRequest istek) {
        Kategori kategori = aktifBul(id);
        surumuDogrula(kategori, istek.versiyon());
        demirbasDurumKoduZorunlu(kategori.getTur(), istek.durumKodu());
        Kategori yeniUst = ustKategori(istek.ustKategoriId(), kategori.getTur());
        donguOlusmadiginiDogrula(kategori, yeniUst);
        mapper.guncelle(istek, kategori);
        kategori.setUst(yeniUst);
        kategori.setDepo(referansService.aktif(Depo.class, istek.depoId(), DEPO_ALANI));
        repository.flush();
        return mapper.cevap(kategori);
    }

    @Override
    protected List<KullanimReferansi> kullanimReferanslari() {
        return KULLANIM_REFERANSLARI;
    }

    private Kategori ustKategori(Long ustKategoriId, MalzemeTuru tur) {
        return referansService.aktifVeAyniTurde(Kategori.class, ustKategoriId, UST_ALANI, tur, Kategori::getTur);
    }

    /**
     * Yeni üstün atalarından biri kategorinin kendisiyse döngü oluşur (422). Legacy veride önceden kalmış bir döngü
     * sonsuz döngüye yol açmasın diye ziyaret edilen düğümler takip edilir.
     */
    private static void donguOlusmadiginiDogrula(Kategori kategori, Kategori yeniUst) {
        Set<Long> ziyaretEdilenler = new HashSet<>();
        for (Kategori ata = yeniUst; ata != null && ziyaretEdilenler.add(ata.getId()); ata = ata.getUst()) {
            if (ata.getId().equals(kategori.getId())) {
                throw new IsKuraliException(DONGU, "Kategori ağacında döngü: id=" + kategori.getId(), yeniUst.getId());
            }
        }
    }

    /** Domain kuralı (CK_KATEGORI_DEMIRBAS): eksikse entity'ye gitmeden 422. */
    private static void demirbasDurumKoduZorunlu(MalzemeTuru tur, String durumKodu) {
        if (tur == MalzemeTuru.DEMIRBAS && durumKodu == null) {
            throw new IsKuraliException(DEMIRBAS_DURUM_KODU_ZORUNLU, "Demirbaş kategorisinde durumKodu zorunlu");
        }
    }
}
