package tr.depo.backend.mapper;

import org.springframework.stereotype.Component;
import tr.depo.backend.dto.IliskiOzetiResponse;
import tr.depo.domain.envanter.Envanter;
import tr.depo.domain.ortak.BaseEntity;
import tr.depo.domain.tanim.Depo;
import tr.depo.domain.tanim.Firma;
import tr.depo.domain.tanim.Ihale;
import tr.depo.domain.tanim.Kategori;
import tr.depo.domain.tanim.Marka;
import tr.depo.domain.tanim.Model;

/**
 * Entity → {@link IliskiOzetiResponse} dönüşümleri; MapStruct bunları türe göre otomatik kullanır. Silinmiş ilişki
 * {@code null}'a çevrilir. Yeni bir ilişki türü özetlenecekse buraya bir metot eklenir.
 */
@Component
public class IliskiOzetiMapper {

    public IliskiOzetiResponse depo(Depo depo) {
        return aktifMi(depo) ? new IliskiOzetiResponse(depo.getId(), depo.getAd()) : null;
    }

    public IliskiOzetiResponse kategori(Kategori kategori) {
        return aktifMi(kategori) ? new IliskiOzetiResponse(kategori.getId(), kategori.getAd()) : null;
    }

    public IliskiOzetiResponse marka(Marka marka) {
        return aktifMi(marka) ? new IliskiOzetiResponse(marka.getId(), marka.getAd()) : null;
    }

    public IliskiOzetiResponse model(Model model) {
        return aktifMi(model) ? new IliskiOzetiResponse(model.getId(), model.getAd()) : null;
    }

    /** Legacy demirbaş firmalarında ad boş olabilir; özet yine döner (id ile). */
    public IliskiOzetiResponse firma(Firma firma) {
        return aktifMi(firma) ? new IliskiOzetiResponse(firma.getId(), firma.getAd()) : null;
    }

    /** Demirbaşlarda ad boş olabilir; özet yine döner (id ile). */
    public IliskiOzetiResponse envanter(Envanter envanter) {
        return aktifMi(envanter) ? new IliskiOzetiResponse(envanter.getId(), envanter.getAd()) : null;
    }

    public IliskiOzetiResponse ihale(Ihale ihale) {
        return aktifMi(ihale) ? new IliskiOzetiResponse(ihale.getId(), ihale.getAd()) : null;
    }

    private static boolean aktifMi(BaseEntity kayit) {
        return kayit != null && !kayit.isSilindi();
    }
}
