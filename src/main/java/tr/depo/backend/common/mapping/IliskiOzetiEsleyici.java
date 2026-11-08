package tr.depo.backend.common.mapping;

import org.springframework.stereotype.Component;
import tr.depo.domain.envanter.Envanter;
import tr.depo.domain.ortak.BaseEntity;
import tr.depo.domain.tanim.Depo;
import tr.depo.domain.tanim.Firma;
import tr.depo.domain.tanim.Ihale;
import tr.depo.domain.tanim.Kategori;
import tr.depo.domain.tanim.Marka;
import tr.depo.domain.tanim.Model;

/**
 * Entity → {@link IliskiOzeti} dönüşümleri; MapStruct bunları türe göre otomatik kullanır. Silinmiş ilişki
 * {@code null}'a çevrilir. Yeni bir ilişki türü özetlenecekse buraya bir metot eklenir.
 */
@Component
public class IliskiOzetiEsleyici {

    public IliskiOzeti depo(Depo depo) {
        return aktifMi(depo) ? new IliskiOzeti(depo.getId(), depo.getAd()) : null;
    }

    public IliskiOzeti kategori(Kategori kategori) {
        return aktifMi(kategori) ? new IliskiOzeti(kategori.getId(), kategori.getAd()) : null;
    }

    public IliskiOzeti marka(Marka marka) {
        return aktifMi(marka) ? new IliskiOzeti(marka.getId(), marka.getAd()) : null;
    }

    public IliskiOzeti model(Model model) {
        return aktifMi(model) ? new IliskiOzeti(model.getId(), model.getAd()) : null;
    }

    /** Legacy demirbaş firmalarında ad boş olabilir; özet yine döner (id ile). */
    public IliskiOzeti firma(Firma firma) {
        return aktifMi(firma) ? new IliskiOzeti(firma.getId(), firma.getAd()) : null;
    }

    /** Demirbaşlarda ad boş olabilir; özet yine döner (id ile). */
    public IliskiOzeti envanter(Envanter envanter) {
        return aktifMi(envanter) ? new IliskiOzeti(envanter.getId(), envanter.getAd()) : null;
    }

    public IliskiOzeti ihale(Ihale ihale) {
        return aktifMi(ihale) ? new IliskiOzeti(ihale.getId(), ihale.getAd()) : null;
    }

    private static boolean aktifMi(BaseEntity kayit) {
        return kayit != null && !kayit.isSilindi();
    }
}
