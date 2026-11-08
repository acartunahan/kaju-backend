package tr.depo.backend.envanter;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.transaction.PlatformTransactionManager;
import tr.depo.backend.support.ApiIstemcisi;
import tr.depo.backend.support.OracleEntegrasyonTestTabani;
import tr.depo.backend.support.fixture.EnvanterFixture;
import tr.depo.backend.support.fixture.VeritabaniFixture;
import tr.depo.domain.tanim.MalzemeTuru;

/** Envanter aggregate'i gerçek Oracle'da: demirbaş/sarf ayrımı, alt kaynaklar ve aggregate soft delete. */
class EnvanterEntegrasyonTesti extends OracleEntegrasyonTestTabani {

    @Autowired
    private MockMvcTester mvc;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private PlatformTransactionManager islemYoneticisi;
    @Autowired
    private DataSource veriKaynagi;

    private VeritabaniFixture veri;
    private JdbcTemplate jdbc;
    private ApiIstemcisi demirbaslar;
    private ApiIstemcisi sarflar;

    @BeforeEach
    void hazirla() {
        veri = new VeritabaniFixture(entityManager, islemYoneticisi);
        jdbc = new JdbcTemplate(veriKaynagi);
        demirbaslar = new ApiIstemcisi(mvc, "/api/v1/demirbaslar");
        sarflar = new ApiIstemcisi(mvc, "/api/v1/sarf-malzemeleri");
    }

    private ApiIstemcisi altKaynak(long envanterId, String ad) {
        return new ApiIstemcisi(mvc, "/api/v1/envanterler/" + envanterId + "/" + ad);
    }

    private boolean silinmisMi(String tablo, long id) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM " + tablo + " WHERE ID = ? AND SILINME_ZAMANI IS NOT NULL",
            Long.class, id) == 1;
    }

    @Test
    void shouldCreateDemirbasWithDefinitionsAndKeepTypesApart() {
        long depoId = veri.depo("Bilgi İşlem Deposu");
        long markaId = veri.marka(MalzemeTuru.DEMIRBAS, "Dell");
        long id = demirbaslar.olustur(EnvanterFixture.demirbas().ile("depoId", depoId).ile("markaId", markaId));

        assertThat(demirbaslar.get(id)).hasStatusOk().bodyJson()
            .hasPathSatisfying("$.marka.ad", m -> assertThat(m).asString().isEqualTo("Dell"))
            .hasPathSatisfying("$.depo.ad", d -> assertThat(d).asString().isEqualTo("Bilgi İşlem Deposu"))
            .hasPathSatisfying("$.zimmetliSicil", s -> assertThat(s).asString().isEqualTo("01234"));
        assertThat(demirbaslar.listedekiIdler()).contains((int) id);
        assertThat(sarflar.listedekiIdler()).doesNotContain((int) id);

        assertThat(sarflar.get(id)).hasStatus(404);
        assertThat(sarflar.put(id, EnvanterFixture.sarf("Toner").guncelleme(0))).hasStatus(404);
        assertThat(sarflar.delete(id)).hasStatus(404);
        assertThat(demirbaslar.get(id)).hasStatusOk();
    }

    @Test
    void shouldRejectSarfDefinitionOnDemirbasAndInvalidWarranty() {
        long sarfMarka = veri.marka(MalzemeTuru.SARF, "Kırtasiye Markası");

        assertThat(demirbaslar.post(EnvanterFixture.demirbas().ile("markaId", sarfMarka)))
            .hasStatus(422).bodyJson().extractingPath("$.alan").isEqualTo("markaId");
        assertThat(demirbaslar.post(EnvanterFixture.demirbas().ile("garantiBitis", "2025-01-01T00:00:00")))
            .hasStatus(422).bodyJson().extractingPath("$.detail")
            .isEqualTo("Garanti bitiş tarihi başlangıç tarihinden önce olamaz.");
    }

    @Test
    void shouldCreateUpdateAndDeleteSarfMalzemesi() {
        long markaId = veri.marka(MalzemeTuru.SARF, "HP Sarf");
        long id = sarflar.olustur(EnvanterFixture.sarf("Toner 85A").ile("markaId", markaId));

        assertThat(sarflar.put(id, EnvanterFixture.sarf("Toner 85A XL").ile("markaId", markaId).guncelleme(0)))
            .hasStatusOk().bodyJson()
            .hasPathSatisfying("$.ad", a -> assertThat(a).asString().isEqualTo("Toner 85A XL"))
            .hasPathSatisfying("$.versiyon", v -> assertThat(v).asNumber().isEqualTo(1));
        assertThat(demirbaslar.get(id)).hasStatus(404);
        assertThat(sarflar.delete(id)).hasStatus(204);
        assertThat(silinmisMi("ENVANTER", id)).isTrue();
    }

    @Test
    void shouldManageChildrenOnlyThroughTheirEnvanter() {
        long envanterId = demirbaslar.olustur(EnvanterFixture.demirbas());
        long baskaEnvanterId = demirbaslar.olustur(EnvanterFixture.demirbas());
        ApiIstemcisi barkodlar = altKaynak(envanterId, "barkodlar");
        long barkodId = barkodlar.olustur(EnvanterFixture.barkod("8690000000001"));
        barkodlar.olustur(EnvanterFixture.barkod("8690000000002").ile("seriNoMu", true));

        assertThat(barkodlar.listedekiIdler()).hasSize(2);
        assertThat(altKaynak(baskaEnvanterId, "barkodlar").get(barkodId)).as("başka envanterin barkodu").hasStatus(404);

        assertThat(barkodlar.put(barkodId, EnvanterFixture.barkod("8690000000009").guncelleme(0))).hasStatusOk();
        assertThat(barkodlar.put(barkodId, EnvanterFixture.barkod("8690000000010").guncelleme(0))).hasStatus(409);

        assertThat(barkodlar.delete(barkodId)).hasStatus(204);
        assertThat(barkodlar.get(barkodId)).hasStatus(404);
        assertThat(barkodlar.listedekiIdler()).hasSize(1);
        assertThat(silinmisMi("BARKOD", barkodId)).isTrue();
    }

    @Test
    void shouldSoftDeleteChildrenWithEnvanterButKeepMovementHistory() {
        long envanterId = demirbaslar.olustur(EnvanterFixture.demirbas());
        long barkodId = altKaynak(envanterId, "barkodlar").olustur(EnvanterFixture.barkod("8690000000100"));
        long notId = altKaynak(envanterId, "notlar").olustur(EnvanterFixture.not("Ekranda çizik var"));
        long aparatId = altKaynak(envanterId, "aparatlar").olustur(EnvanterFixture.aparat("Şarj adaptörü", 1));
        long hareketId = veri.demirbasHareketi(envanterId);

        assertThat(demirbaslar.delete(envanterId)).hasStatus(204);

        assertThat(silinmisMi("ENVANTER", envanterId)).isTrue();
        assertThat(silinmisMi("BARKOD", barkodId)).isTrue();
        assertThat(silinmisMi("ENVANTER_NOTU", notId)).isTrue();
        assertThat(silinmisMi("ENVANTER_APARATI", aparatId)).isTrue();
        assertThat(silinmisMi("ENVANTER_HAREKETI", hareketId)).as("hareket geçmişi silinmez").isFalse();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM BARKOD WHERE ID = ?", Long.class, barkodId))
            .as("fiziksel silme yok").isEqualTo(1);

        assertThat(altKaynak(envanterId, "aparatlar").get(aparatId)).hasStatus(404);
        assertThat(altKaynak(envanterId, "notlar").post(EnvanterFixture.not("yeni"))).hasStatus(404);
    }

    @Test
    void shouldRejectAparatCountBelowOneAndKeepNoteSicilTrimmed() {
        long envanterId = demirbaslar.olustur(EnvanterFixture.demirbas());

        assertThat(altKaynak(envanterId, "aparatlar").post(EnvanterFixture.aparat("Kablo", 0))).hasStatus(400);
        long notId = altKaynak(envanterId, "notlar").olustur(EnvanterFixture.not("Not").ile("ekleyenSicil", " 0099 "));
        assertThat(altKaynak(envanterId, "notlar").get(notId)).hasStatusOk()
            .bodyJson().extractingPath("$.ekleyenSicil").isEqualTo("0099");
    }
}
