package tr.depo.backend.config;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA ayarları.
 *
 * <p>Entity'ler bu projede değil, {@code tr.depo:depo-domain} bağımlılığındadır ({@code tr.depo.domain}); bu yüzden
 * entity taraması açıkça verilir. Audit zamanları domain'in sözleşmesi gereği UTC duvar saatidir
 * ({@code LocalDateTime}, saat dilimi bilgisi yok).
 */
@Configuration
@EntityScan(basePackages = JpaConfig.DOMAIN_PAKETI)
@EnableJpaRepositories(basePackages = JpaConfig.UYGULAMA_PAKETI)
@EnableJpaAuditing(auditorAwareRef = "denetciSaglayici", dateTimeProviderRef = "utcZamanSaglayici")
public class JpaConfig {

    static final String DOMAIN_PAKETI = "tr.depo.domain";
    static final String UYGULAMA_PAKETI = "tr.depo.backend";

    @Bean
    public Clock saat() {
        return Clock.systemUTC();
    }

    @Bean
    public DateTimeProvider utcZamanSaglayici(Clock saat) {
        return () -> Optional.of(LocalDateTime.now(saat));
    }
}
