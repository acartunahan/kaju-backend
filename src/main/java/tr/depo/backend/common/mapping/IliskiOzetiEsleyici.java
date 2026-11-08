package tr.depo.backend.common.mapping;

import org.springframework.stereotype.Component;
import tr.depo.domain.ortak.BaseEntity;
import tr.depo.domain.tanim.Depo;

/**
 * Entity → {@link IliskiOzeti} dönüşümleri; MapStruct bunları türe göre otomatik kullanır. Silinmiş ilişki
 * {@code null}'a çevrilir. Yeni bir ilişki türü özetlenecekse buraya bir metot eklenir.
 */
@Component
public class IliskiOzetiEsleyici {

    public IliskiOzeti depo(Depo depo) {
        return aktifMi(depo) ? new IliskiOzeti(depo.getId(), depo.getAd()) : null;
    }

    private static boolean aktifMi(BaseEntity kayit) {
        return kayit != null && !kayit.isSilindi();
    }
}
