package tr.depo.backend.util;

import tr.depo.backend.exception.KayitBulunamadiException;
import tr.depo.backend.repository.AktifKayitRepository;
import tr.depo.domain.ortak.BaseEntity;

/**
 * "Silinmemiş kaydı bul, yoksa 404" kuralı tek yerde. Domain'deki {@code silinmemis} filtresi ID ile yüklemeye
 * uygulanmadığı için {@code findById} silinmiş kaydı da döndürür; servisler kaydı her zaman buradan alır.
 */
public final class AktifKayitUtil {

    private AktifKayitUtil() {
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
