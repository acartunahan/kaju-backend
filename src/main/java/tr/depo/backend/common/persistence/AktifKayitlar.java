package tr.depo.backend.common.persistence;

import tr.depo.backend.common.exception.KayitBulunamadiException;
import tr.depo.domain.ortak.BaseEntity;

/**
 * "Silinmemiş kaydı bul, yoksa 404" kuralı tek yerde. Domain'deki {@code silinmemis} filtresi ID ile yüklemeye
 * uygulanmadığı için {@code findById} silinmiş kaydı da döndürür; servisler kaydı her zaman buradan alır.
 */
public final class AktifKayitlar {

    private AktifKayitlar() {
    }

    /**
     * @param kaynakAdi hata mesajında görünecek kaynak adı (ör. "Marka")
     * @throws KayitBulunamadiException kayıt yoksa ya da silinmişse (404)
     */
    public static <T extends BaseEntity> T bul(AktifKayitRepository<T> repository, Long id, String kaynakAdi) {
        return repository.findById(id)
            .filter(kayit -> !kayit.isSilindi())
            .orElseThrow(() -> new KayitBulunamadiException(kaynakAdi, id));
    }
}
