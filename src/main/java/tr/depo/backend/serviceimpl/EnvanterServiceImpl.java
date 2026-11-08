package tr.depo.backend.serviceimpl;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import tr.depo.backend.exception.IsKuraliException;
import tr.depo.backend.exception.ReferansGecersizException;
import tr.depo.backend.service.EnvanterService;
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

    private static final String UST_ALANI = "envanter";
    /** Fotoğrafın API'si henüz yok ama aggregate'e aittir; tabloda kaydı varsa o da silinir. */
    private static final List<Class<? extends BaseEntity>> ALT_KAYIT_TURLERI =
        List.of(Barkod.class, EnvanterNotu.class, EnvanterAparati.class, EnvanterFotografi.class);

    private final ReferansService referansService;
    private final SilmeService silmeService;

    public EnvanterServiceImpl(ReferansService referansService, SilmeService silmeService) {
        this.referansService = referansService;
        this.silmeService = silmeService;
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
