package tr.depo.backend.common.config;

import java.util.Optional;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

/**
 * Kimlik doğrulama eklenene kadar geçici denetçi: tüm kayıtlarda OLUSTURAN / GUNCELLEYEN / SILEN değeri sabittir.
 * Kimlik doğrulama eklendiğinde bu sınıf, oturumdaki kullanıcının sicilini döndüren bir uygulamayla değiştirilir
 * (en fazla 36 karakter; domain'deki audit kolonlarının uzunluğu).
 */
@Component("denetciSaglayici")
public class GeciciAuditorAware implements AuditorAware<String> {

    static final String GECICI_DENETCI = "API";

    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.of(GECICI_DENETCI);
    }
}
