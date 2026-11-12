package tr.depo.backend;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import tr.depo.backend.util.OracleEntegrasyonTestTabani;
import tr.depo.backend.util.OracleTestOrtami;

/**
 * Uygulama gerçek Oracle'da ayağa kalkıyor mu: domain jar'ındaki Flyway şeması kuruluyor, Hibernate
 * {@code ddl-auto=validate} geçiyor (bağlam açıldıysa geçmiştir), health/OpenAPI çalışıyor ve hata formatı doğru.
 */
class UygulamaEntegrasyonTesti extends OracleEntegrasyonTestTabani {

    @Autowired
    private MockMvcTester mvc;

    @Autowired
    private Flyway flyway;

    @Test
    void shouldApplyDomainFlywaySchemaWithSoftDeleteTriggers() throws Exception {
        try (Connection baglanti = OracleTestOrtami.baglanti(); Statement ifade = baglanti.createStatement()) {
            assertThat(tekSayi(ifade, "SELECT COUNT(*) FROM \"flyway_schema_history\" WHERE \"version\" IN ('1', '2') "
                + "AND \"success\" = 1")).isEqualTo(2);
            assertThat(tekSayi(ifade, "SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME IN ('ENVANTER', 'MARKA', 'SAYIM_SATIRI', "
                + "'ZIMMET_GECMISI')")).isEqualTo(4);
            // 20 domain tablosu: V1 (19) + V2 (ZIMMET_GECMISI); her birinde silme yasağı trigger'ı
            assertThat(tekSayi(ifade, "SELECT COUNT(*) FROM USER_TRIGGERS WHERE TRIGGER_NAME LIKE 'TRG\\_%\\_SILME\\_YASAK' ESCAPE '\\' "
                + "AND STATUS = 'ENABLED'")).isEqualTo(20);
            assertThat(isimler(ifade, "SELECT TABLE_NAME FROM USER_TABLES WHERE TABLE_NAME LIKE 'MIG\\_%' ESCAPE '\\' "
                + "OR TABLE_NAME LIKE 'BATCH\\_%' ESCAPE '\\'")).as("göç tabloları bu projede kurulmaz").isEmpty();
        }
    }

    /**
     * Migration uygulaması aynı şemaya V1000/V1001 (göç tabloları) uygular. Bu script'ler backend'de yok ve
     * domain'in son sürümünden büyük oldukları için Flyway onları "future" sayar. ignore-migration-patterns
     * ayarı Flyway'in varsayılanı olan "*:future"ı ezdiği için ikisi de açıkça yazılmalı.
     */
    @Test
    void shouldValidateSchemaMigratedByMigrationApp() throws Exception {
        try (Connection baglanti = OracleTestOrtami.baglanti(); Statement ifade = baglanti.createStatement()) {
            for (String surum : List.of("1000", "1001")) {
                ifade.executeUpdate("INSERT INTO \"flyway_schema_history\" (\"installed_rank\", \"version\", \"description\", "
                    + "\"type\", \"script\", \"checksum\", \"installed_by\", \"installed_on\", \"execution_time\", \"success\") "
                    + "SELECT MAX(\"installed_rank\") + 1, '" + surum + "', 'goc', 'SQL', 'V" + surum + "__goc.sql', 1, USER, "
                    + "SYSTIMESTAMP, 0, 1 FROM \"flyway_schema_history\"");
            }
            try {
                assertThat(flyway.validateWithResult().validationSuccessful).isTrue();
            } finally {
                ifade.executeUpdate("DELETE FROM \"flyway_schema_history\" WHERE \"version\" IN ('1000', '1001')");
            }
        }
    }

    @Test
    void shouldReportHealthUp() {
        assertThat(mvc.get().uri("/actuator/health"))
            .hasStatusOk()
            .bodyJson().extractingPath("$.status").isEqualTo("UP");
    }

    @Test
    void shouldServeOpenApiDocument() {
        assertThat(mvc.get().uri("/v3/api-docs"))
            .hasStatusOk()
            .bodyJson().extractingPath("$.info.title").isEqualTo("Kaju Backend API");
    }

    @Test
    void shouldReturnProblemDetailForUnknownPath() {
        assertThat(mvc.get().uri("/api/v1/olmayan-kaynak").accept(MediaType.APPLICATION_JSON))
            .hasStatus(404)
            .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
            .bodyJson().extractingPath("$.kod").isEqualTo("KAYNAK_BULUNAMADI");
    }

    private static long tekSayi(Statement ifade, String sql) throws Exception {
        try (ResultSet sonuc = ifade.executeQuery(sql)) {
            sonuc.next();
            return sonuc.getLong(1);
        }
    }

    private static List<String> isimler(Statement ifade, String sql) throws Exception {
        List<String> liste = new ArrayList<>();
        try (ResultSet sonuc = ifade.executeQuery(sql)) {
            while (sonuc.next()) {
                liste.add(sonuc.getString(1));
            }
        }
        return liste;
    }
}
