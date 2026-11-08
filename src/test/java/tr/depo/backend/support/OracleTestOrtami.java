package tr.depo.backend.support;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HexFormat;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.oracle.OracleContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Entegrasyon testleri için tek bir Oracle Free konteyneri (JVM başına bir kez başlar).
 *
 * <p>Uygulama, üretimdeki gibi şema sahibi bir kullanıcıyla ({@code HEDEF}) bağlanır ve şemayı Flyway kurar.
 * Kullanıcının yetkileri canlı runbook'uyla aynıdır; {@code CREATE TRIGGER} domain'deki fiziksel silme yasağı
 * trigger'ları için gereklidir. Şifre her çalıştırmada rastgele üretilir.
 *
 * <p>Kapalı ağda imaj kurum registry'sinden: {@code -Dtest.oracle.imaj=registry.kurum/gvenzl/oracle-free:23-slim-faststart}.
 */
public final class OracleTestOrtami {

    private static final String KULLANICI = "HEDEF";
    private static final String SIFRE = "T" + HexFormat.of().formatHex(rastgeleBaytlar());
    private static final String IMAJ = System.getProperty("test.oracle.imaj", "gvenzl/oracle-free:23-slim-faststart");

    private static final OracleContainer ORACLE = new OracleContainer(
        DockerImageName.parse(IMAJ).asCompatibleSubstituteFor("gvenzl/oracle-free"));

    private static boolean hazir;

    private OracleTestOrtami() {
    }

    public static synchronized void baslat() {
        if (hazir) {
            return;
        }
        ORACLE.start();
        try (Connection sistem = DriverManager.getConnection(ORACLE.getJdbcUrl(), "system", ORACLE.getPassword());
             Statement ifade = sistem.createStatement()) {
            ifade.execute("CREATE USER " + KULLANICI + " IDENTIFIED BY \"" + SIFRE + "\" QUOTA UNLIMITED ON USERS");
            ifade.execute("GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE, CREATE VIEW, CREATE TRIGGER TO " + KULLANICI);
        } catch (SQLException hata) {
            throw new IllegalStateException("Test veritabanı kullanıcısı oluşturulamadı", hata);
        }
        hazir = true;
    }

    public static void ozellikler(DynamicPropertyRegistry kayit) {
        baslat();
        kayit.add("spring.datasource.url", ORACLE::getJdbcUrl);
        kayit.add("spring.datasource.username", () -> KULLANICI);
        kayit.add("spring.datasource.password", () -> SIFRE);
    }

    public static Connection baglanti() throws SQLException {
        baslat();
        return DriverManager.getConnection(ORACLE.getJdbcUrl(), KULLANICI, SIFRE);
    }

    private static byte[] rastgeleBaytlar() {
        byte[] baytlar = new byte[12];
        new SecureRandom().nextBytes(baytlar);
        return baytlar;
    }
}
