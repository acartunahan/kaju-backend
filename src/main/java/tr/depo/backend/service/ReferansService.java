package tr.depo.backend.service;

import java.util.function.Function;
import tr.depo.domain.ortak.BaseEntity;
import tr.depo.domain.tanim.MalzemeTuru;

/**
 * İstekte ID ile verilen ilişkili kaydı yükler ve aktif (silinmemiş) olduğunu doğrular. Her servise ilişkili her
 * entity'nin repository'sini enjekte etmek yerine tek bir yerde yapılır.
 */
public interface ReferansService {

    /**
     * @param alan istekteki alan adı (hata cevabında döner, ör. {@code depoId})
     * @return kayıt; {@code id} null ise null (isteğe bağlı ilişki — zorunluluk DTO doğrulamasının işidir)
     * @throws tr.depo.backend.exception.ReferansGecersizException kayıt yoksa ya da silinmişse (422)
     */
    <T extends BaseEntity> T aktif(Class<T> tip, Long id, String alan);

    /**
     * {@link #aktif} ve ek olarak tanımın türü (DEMIRBAS/SARF) beklenenle aynı olmalı.
     *
     * @throws tr.depo.backend.exception.ReferansGecersizException kayıt yok, silinmiş ya da türü farklıysa (422)
     */
    <T extends BaseEntity> T aktifVeAyniTurde(Class<T> tip, Long id, String alan, MalzemeTuru beklenenTur,
                                              Function<T, MalzemeTuru> turu);
}
