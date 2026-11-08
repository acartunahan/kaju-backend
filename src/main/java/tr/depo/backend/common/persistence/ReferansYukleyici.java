package tr.depo.backend.common.persistence;

import jakarta.persistence.EntityManager;
import java.util.function.Function;
import org.springframework.stereotype.Component;
import tr.depo.backend.common.exception.ReferansGecersizException;
import tr.depo.domain.ortak.BaseEntity;
import tr.depo.domain.tanim.MalzemeTuru;

/**
 * İstekte ID ile verilen ilişkili kaydı yükler ve aktif (silinmemiş) olduğunu doğrular. Her servise ilişkili her
 * entity'nin repository'sini enjekte etmek yerine tek bir yerde yapılır.
 */
@Component
public class ReferansYukleyici {

    private final EntityManager entityManager;

    public ReferansYukleyici(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * @param alan istekteki alan adı (hata cevabında döner, ör. {@code depoId})
     * @return kayıt; {@code id} null ise null (isteğe bağlı ilişki — zorunluluk DTO doğrulamasının işidir)
     * @throws ReferansGecersizException kayıt yoksa ya da silinmişse (422)
     */
    public <T extends BaseEntity> T aktif(Class<T> tip, Long id, String alan) {
        if (id == null) {
            return null;
        }
        T kayit = entityManager.find(tip, id);
        if (kayit == null || kayit.isSilindi()) {
            throw new ReferansGecersizException(alan, id);
        }
        return kayit;
    }

    /**
     * {@link #aktif} ve ek olarak tanımın türü (DEMIRBAS/SARF) beklenenle aynı olmalı. Domain, türler arası bağlantıyı
     * entity'de ve veritabanında da reddeder; burada önceden kontrol edilerek 422 ve alan bilgisiyle döner.
     *
     * @throws ReferansGecersizException kayıt yok, silinmiş ya da türü farklıysa (422)
     */
    public <T extends BaseEntity> T aktifVeAyniTurde(Class<T> tip, Long id, String alan, MalzemeTuru beklenenTur,
                                                     Function<T, MalzemeTuru> turu) {
        T kayit = aktif(tip, id, alan);
        if (kayit != null && turu.apply(kayit) != beklenenTur) {
            throw ReferansGecersizException.turUyumsuz(alan, id, beklenenTur);
        }
        return kayit;
    }
}
