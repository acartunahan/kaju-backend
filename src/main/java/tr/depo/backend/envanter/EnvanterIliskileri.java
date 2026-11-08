package tr.depo.backend.envanter;

import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import tr.depo.backend.common.exception.IsKuraliException;
import tr.depo.backend.common.exception.ReferansGecersizException;
import tr.depo.backend.common.persistence.ReferansYukleyici;
import tr.depo.domain.envanter.Envanter;
import tr.depo.domain.tanim.Depo;
import tr.depo.domain.tanim.Ihale;
import tr.depo.domain.tanim.Kategori;
import tr.depo.domain.tanim.Marka;
import tr.depo.domain.tanim.Model;

/**
 * Demirbaş ve sarf servislerinin ortak işi: envanterin tanım ilişkilerini ID'den çözüp bağlamak (kategori, marka,
 * model envanterle aynı türde olmalı; model seçilen markaya ait olmalı) ve garanti tarihlerini doğrulamak.
 */
@Component
public class EnvanterIliskileri {

    static final String GARANTI_SIRASI = "envanter.garanti-sirasi";
    static final String MODEL_MARKA_UYUMSUZ = "envanter.model-marka-uyumsuz";

    private final ReferansYukleyici referansYukleyici;

    public EnvanterIliskileri(ReferansYukleyici referansYukleyici) {
        this.referansYukleyici = referansYukleyici;
    }

    /** Tüm tanım ilişkilerini çözer ve bağlar; herhangi biri geçersizse hiçbiri bağlanmadan 422. */
    public void bagla(Envanter envanter, Long kategoriId, Long markaId, Long modelId, Long depoId, Long ihaleId) {
        Kategori kategori = referansYukleyici.aktifVeAyniTurde(Kategori.class, kategoriId, "kategoriId",
            envanter.getTur(), Kategori::getTur);
        Marka marka = referansYukleyici.aktifVeAyniTurde(Marka.class, markaId, "markaId", envanter.getTur(), Marka::getTur);
        Model model = referansYukleyici.aktifVeAyniTurde(Model.class, modelId, "modelId", envanter.getTur(), Model::getTur);
        marka = modelinMarkasi(model, marka);
        Depo depo = referansYukleyici.aktif(Depo.class, depoId, "depoId");
        Ihale ihale = referansYukleyici.aktif(Ihale.class, ihaleId, "ihaleId");
        envanter.setKategori(kategori);
        envanter.setMarka(marka);
        envanter.setModel(model);
        envanter.setDepo(depo);
        envanter.setIhale(ihale);
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

    /**
     * Garanti bitişi başlangıçtan önce olamaz. Domain bu kuralı legacy veri bozulmasın diye entity'de zorlamaz ve yeni
     * kayıtlar için servise bırakır ({@code GarantiSuresi} javadoc'u).
     */
    public static void garantiSirasiniDogrula(LocalDateTime baslangic, LocalDateTime bitis) {
        if (baslangic != null && bitis != null && bitis.isBefore(baslangic)) {
            throw new IsKuraliException(GARANTI_SIRASI, "Garanti bitişi başlangıçtan önce");
        }
    }
}
