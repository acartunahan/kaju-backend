package tr.depo.backend.util;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Oracle'a karşı çalışan entegrasyon testlerinin tabanı: tüm uygulama bağlamı, gerçek Flyway şeması
 * ({@code ddl-auto=validate} dahil) ve MockMvc. Tüm alt sınıflar aynı bağlamı ve konteyneri paylaşır.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class OracleEntegrasyonTestTabani {

    @DynamicPropertySource
    static void veritabani(DynamicPropertyRegistry kayit) {
        OracleTestOrtami.ozellikler(kayit);
    }
}
