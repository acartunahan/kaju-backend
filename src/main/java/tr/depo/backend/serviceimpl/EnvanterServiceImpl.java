package tr.depo.backend.serviceimpl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import tr.depo.backend.dto.EnvanterAramaKriteri;
import tr.depo.backend.exception.IsKuraliException;
import tr.depo.backend.exception.KayitBulunamadiException;
import tr.depo.backend.exception.ReferansGecersizException;
import tr.depo.backend.repository.EnvanterRepository;
import tr.depo.backend.repository.KategoriRepository;
import tr.depo.backend.repository.KosulluSorguRepository;
import tr.depo.backend.service.EnvanterService;
import tr.depo.backend.util.AktifKayitUtil;
import tr.depo.domain.tanim.MalzemeTuru;
import tr.depo.backend.service.ReferansService;
import tr.depo.backend.service.SilmeService;
import tr.depo.domain.envanter.Barkod;
import tr.depo.domain.envanter.Envanter;
import tr.depo.domain.envanter.EnvanterAparati;
import tr.depo.domain.envanter.EnvanterFotografi;
import tr.depo.domain.envanter.EnvanterNotu;
import tr.depo.domain.ortak.BaseEntity;
import tr.depo.domain.tanim.Depo;
import tr.depo.domain.tanim.Ihale;
import tr.depo.domain.tanim.Kategori;
import tr.depo.domain.tanim.Marka;
import tr.depo.domain.tanim.Model;

/**
 * Demirbaş ve sarf servislerinin ortak işi: envanterin tanım ilişkilerini ID'den çözüp bağlamak (kategori, marka,
 * model envanterle aynı türde olmalı; model seçilen markaya ait olmalı), garanti tarihlerini doğrulamak ve envanter
 * silinirken alt kayıtlarını soft delete etmek.
 */
@Service
public class EnvanterServiceImpl implements EnvanterService {

    /** Envanter alt kaynaklarında (barkod, not, aparat) üst kaydın hata mesajındaki adı. */
    static final String KAYNAK = "Envanter";
    static final String GARANTI_SIRASI = "envanter.garanti-sirasi";
    static final String MODEL_MARKA_UYUMSUZ = "envanter.model-marka-uyumsuz";
    static final String TANIM_BOSALTILAMAZ = "envanter.tanim-bosaltilamaz";

    private static final String UST_ALANI = "envanter";
    /** Fotoğrafın API'si henüz yok ama aggregate'e aittir; tabloda kaydı varsa o da silinir. */
    private static final List<Class<? extends BaseEntity>> ALT_KAYIT_TURLERI =
        List.of(Barkod.class, EnvanterNotu.class, EnvanterAparati.class, EnvanterFotografi.class);

    /** Cevapta özetlenen to-one ilişkiler; listede birlikte okunur (N+1 yok). */
    private static final String[] OZETLENEN_ILISKILER = {"kategori", "marka", "model", "depo", "ihale"};

    private final ReferansService referansService;
    private final SilmeService silmeService;
    private final EnvanterRepository envanterRepository;
    private final KategoriRepository kategoriRepository;
    private final KosulluSorguRepository kosulluSorgu;

    public EnvanterServiceImpl(ReferansService referansService, SilmeService silmeService,
                               EnvanterRepository envanterRepository, KategoriRepository kategoriRepository,
                               KosulluSorguRepository kosulluSorgu) {
        this.referansService = referansService;
        this.silmeService = silmeService;
        this.envanterRepository = envanterRepository;
        this.kategoriRepository = kategoriRepository;
        this.kosulluSorgu = kosulluSorgu;
    }

    @Override
    public Envanter aktifBul(Long id, MalzemeTuru tur, String kaynakAdi) {
        Envanter envanter = AktifKayitUtil.bul(envanterRepository, id, kaynakAdi);
        if (envanter.getTur() != tur) {
            throw new KayitBulunamadiException(kaynakAdi, id);
        }
        return envanter;
    }

    @Override
    public Page<Envanter> ara(EnvanterAramaKriteri kriter, Pageable sayfa) {
        return kosulluSorgu.sayfa(Envanter.class, kosul(kriter), sayfa, OZETLENEN_ILISKILER);
    }

    @Override
    public List<Envanter> bul(EnvanterAramaKriteri kriter, int enFazla) {
        return kosulluSorgu.liste(Envanter.class, kosul(kriter), Sort.by("id"), enFazla, OZETLENEN_ILISKILER);
    }

    private KosulluSorguRepository.Kosul<Envanter> kosul(EnvanterAramaKriteri k) {
        Set<Long> kategoriIdleri = kategoriIdleri(k);
        return (cb, kok, sorgu) -> {
            List<Predicate> kosullar = new ArrayList<>();
            kosullar.add(cb.equal(kok.get("tur"), k.tur()));
            if (kategoriIdleri != null) {
                kosullar.add(kok.get("kategori").get("id").in(kategoriIdleri));
            }
            esitse(kosullar, cb, kok.get("marka").get("id"), k.markaId());
            esitse(kosullar, cb, kok.get("model").get("id"), k.modelId());
            esitse(kosullar, cb, kok.get("depo").get("id"), k.depoId());
            esitse(kosullar, cb, kok.get("ihale").get("id"), k.ihaleId());
            esitse(kosullar, cb, kok.get("durum"), k.durum());
            esitse(kosullar, cb, kok.get("zimmetliSicil").get("deger"),
                k.zimmetliSicil() == null ? null : k.zimmetliSicil().deger());
            esitse(kosullar, cb, kok.get("seriNo"), k.seriNo());
            esitse(kosullar, cb, kok.get("demirbasNo"), k.demirbasNo());
            if (k.barkod() != null) {
                kosullar.add(cb.exists(barkodlari(cb, kok, sorgu, b -> cb.equal(b.get("barkod"), k.barkod()))));
            }
            if (k.q() != null) {
                kosullar.add(metinAramasi(cb, kok, sorgu, k.q()));
            }
            return kosullar;
        };
    }

    /** Ad, seri no, açıklama ve barkodda "içerir"; metin sayıysa demirbaş no da. */
    private static Predicate metinAramasi(CriteriaBuilder cb, Root<Envanter> kok, CriteriaQuery<?> sorgu, String q) {
        List<Predicate> veya = new ArrayList<>(List.of(
            KosulluSorguRepository.icerir(cb, kok.get("ad"), q),
            KosulluSorguRepository.icerir(cb, kok.get("seriNo"), q),
            KosulluSorguRepository.icerir(cb, kok.get("aciklama"), q),
            cb.exists(barkodlari(cb, kok, sorgu, b -> KosulluSorguRepository.icerir(cb, b.get("barkod"), q)))));
        sayi(q).ifPresent(no -> veya.add(cb.equal(kok.get("demirbasNo"), no)));
        return cb.or(veya.toArray(Predicate[]::new));
    }

    /** Envanterin silinmemiş barkodlarından koşula uyanlar (EXISTS için). */
    private static Subquery<Long> barkodlari(CriteriaBuilder cb, Root<Envanter> kok, CriteriaQuery<?> sorgu,
                                             Function<Root<Barkod>, Predicate> kosul) {
        Subquery<Long> alt = sorgu.subquery(Long.class);
        Root<Barkod> barkod = alt.from(Barkod.class);
        alt.select(barkod.get("id"))
            .where(cb.equal(barkod.get("envanter"), kok), cb.isNull(barkod.get("silinmeZamani")), kosul.apply(barkod));
        return alt;
    }

    /** Kategori ve (istenirse) tüm alt kategorileri; ağaçta döngü olsa da her kategori bir kez gezilir. */
    private Set<Long> kategoriIdleri(EnvanterAramaKriteri k) {
        if (k.kategoriId() == null) {
            return null;
        }
        Set<Long> hepsi = new LinkedHashSet<>(List.of(k.kategoriId()));
        List<Long> sinir = List.of(k.kategoriId());
        while (k.altKategoriler() && !sinir.isEmpty()) {
            sinir = kategoriRepository.altKategoriIdleri(sinir).stream().filter(hepsi::add).toList();
        }
        return hepsi;
    }

    private static void esitse(List<Predicate> kosullar, CriteriaBuilder cb, Path<?> alan, Object deger) {
        if (deger != null) {
            kosullar.add(cb.equal(alan, deger));
        }
    }

    private static Optional<Long> sayi(String metin) {
        try {
            return Optional.of(Long.valueOf(metin));
        } catch (NumberFormatException sayiDegil) {
            return Optional.empty();
        }
    }

    @Override
    public void bagla(Envanter envanter, Long kategoriId, Long markaId, Long modelId, Long depoId, Long ihaleId) {
        Kategori kategori = referansService.aktifVeAyniTurde(Kategori.class, kategoriId, "kategoriId",
            envanter.getTur(), Kategori::getTur);
        Marka marka = referansService.aktifVeAyniTurde(Marka.class, markaId, "markaId", envanter.getTur(), Marka::getTur);
        Model model = referansService.aktifVeAyniTurde(Model.class, modelId, "modelId", envanter.getTur(), Model::getTur);
        marka = modelinMarkasi(model, marka);
        Depo depo = referansService.aktif(Depo.class, depoId, "depoId");
        Ihale ihale = referansService.aktif(Ihale.class, ihaleId, "ihaleId");
        envanter.setKategori(kategori);
        envanter.setMarka(marka);
        envanter.setModel(model);
        envanter.setDepo(depo);
        envanter.setIhale(ihale);
    }

    /**
     * "Dolu" = bağlı ve silinmemiş. Silinmiş bir tanım cevapta zaten boş görünür (ilişki özeti null); istemci onu boş
     * gönderdiğinde engellenmez. Kontrol bağlamadan sonra yapılır: marka boş gönderilip modelden doldurulursa boşaltma
     * sayılmaz. İhlalde istisna transaction'ı geri alır; yapılan bağlama kaydedilmez.
     */
    @Override
    public void guncellemedeBagla(Envanter envanter, Long kategoriId, Long markaId, Long modelId, Long depoId,
                                  Long ihaleId) {
        boolean kategoriDolu = dolu(envanter.getKategori());
        boolean markaDolu = dolu(envanter.getMarka());
        boolean modelDolu = dolu(envanter.getModel());
        bagla(envanter, kategoriId, markaId, modelId, depoId, ihaleId);
        bosaltilmamali(kategoriDolu, envanter.getKategori(), "kategoriId");
        bosaltilmamali(markaDolu, envanter.getMarka(), "markaId");
        bosaltilmamali(modelDolu, envanter.getModel(), "modelId");
    }

    private static boolean dolu(BaseEntity tanim) {
        return tanim != null && !tanim.isSilindi();
    }

    private static void bosaltilmamali(boolean onceDolu, BaseEntity simdiki, String alan) {
        if (onceDolu && simdiki == null) {
            throw new IsKuraliException(TANIM_BOSALTILAMAZ, alan + " boşaltılamaz (kayıtta dolu)", alan);
        }
    }

    /**
     * Garanti bitişi başlangıçtan önce olamaz. Domain bu kuralı legacy veri bozulmasın diye entity'de zorlamaz ve yeni
     * kayıtlar için servise bırakır ({@code GarantiSuresi} javadoc'u).
     */
    @Override
    public void garantiSirasiniDogrula(LocalDateTime baslangic, LocalDateTime bitis) {
        if (baslangic != null && bitis != null && bitis.isBefore(baslangic)) {
            throw new IsKuraliException(GARANTI_SIRASI, "Garanti bitişi başlangıçtan önce");
        }
    }

    @Override
    public void altKayitlariSil(Envanter envanter, String silen) {
        ALT_KAYIT_TURLERI.forEach(tur -> silmeService.aktifAltKayitlariSil(tur, UST_ALANI, envanter, silen));
    }

    /**
     * Envanterin markası, modelin markası olmalı. Marka boş bırakılırsa modelin (silinmemiş) markası kullanılır;
     * farklı bir marka seçilmişse 422 ({@code alan = modelId}). Markası olmayan (legacy) modelde kontrol yapılmaz.
     */
    private static Marka modelinMarkasi(Model model, Marka secilenMarka) {
        if (model == null || model.getMarka() == null) {
            return secilenMarka;
        }
        Marka modelinMarkasi = model.getMarka();
        if (secilenMarka == null) {
            return modelinMarkasi.isSilindi() ? null : modelinMarkasi;
        }
        if (!modelinMarkasi.getId().equals(secilenMarka.getId())) {
            throw ReferansGecersizException.iliskiUyumsuz("modelId", model.getId(), MODEL_MARKA_UYUMSUZ);
        }
        return secilenMarka;
    }
}
