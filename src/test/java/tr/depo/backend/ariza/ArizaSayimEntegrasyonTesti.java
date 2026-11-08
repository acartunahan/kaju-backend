package tr.depo.backend.ariza;

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
import tr.depo.backend.support.fixture.ArizaSayimFixture;
import tr.depo.backend.support.fixture.VeritabaniFixture;
import tr.depo.domain.envanter.Envanter;

/** Arıza ve sayım aggregate'leri gerçek Oracle'da: alt kaynaklar, türe göre kurallar ve aggregate soft delete. */
class ArizaSayimEntegrasyonTesti extends OracleEntegrasyonTestTabani {

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
    private ApiIstemcisi arizalar;
    private ApiIstemcisi sayimlar;

    @BeforeEach
    void hazirla() {
        veri = new VeritabaniFixture(entityManager, islemYoneticisi);
        jdbc = new JdbcTemplate(veriKaynagi);
        arizalar = new ApiIstemcisi(mvc, "/api/v1/arizalar");
        sayimlar = new ApiIstemcisi(mvc, "/api/v1/sayimlar");
    }

    private ApiIstemcisi islemler(long arizaId) {
        return new ApiIstemcisi(mvc, "/api/v1/arizalar/" + arizaId + "/islemler");
    }

    private ApiIstemcisi satirlar(long sayimId) {
        return new ApiIstemcisi(mvc, "/api/v1/sayimlar/" + sayimId + "/satirlar");
    }

    private boolean silinmisMi(String tablo, long id) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM " + tablo + " WHERE ID = ? AND SILINME_ZAMANI IS NOT NULL",
            Long.class, id) == 1;
    }

    @Test
    void shouldRecordArizaWithOperationsAndDeleteThemTogether() {
        long envanterId = veri.demirbasEnvanter(e -> e.setAd("Yazıcı"));
        long arizaId = arizalar.olustur(ArizaSayimFixture.ariza().ile("envanterId", envanterId));
        long islem1 = islemler(arizaId).olustur(ArizaSayimFixture.arizaIslemi());
        long islem2 = islemler(arizaId).olustur(ArizaSayimFixture.arizaIslemi().ile("tur", "BILGI").ile("siraNo", 2));

        assertThat(arizalar.get(arizaId)).hasStatusOk().bodyJson()
            .hasPathSatisfying("$.envanter.ad", ad -> assertThat(ad).asString().isEqualTo("Yazıcı"))
            .hasPathSatisfying("$.bildirenSicil", s -> assertThat(s).asString().isEqualTo("0012345"));
        assertThat(islemler(arizaId).listedekiIdler()).containsExactlyInAnyOrder((int) islem1, (int) islem2);

        assertThat(arizalar.delete(arizaId)).hasStatus(204);
        assertThat(silinmisMi("ARIZA", arizaId)).isTrue();
        assertThat(silinmisMi("ARIZA_ISLEMI", islem1)).isTrue();
        assertThat(silinmisMi("ARIZA_ISLEMI", islem2)).isTrue();
        assertThat(islemler(arizaId).get(islem1)).hasStatus(404);
        assertThat(silinmisMi("ENVANTER", envanterId)).as("arıza silinince envanter silinmez").isFalse();
    }

    @Test
    void shouldRejectArizaForDeletedEnvanter() {
        long envanterId = veri.demirbasEnvanter(e -> { });
        veri.sil(Envanter.class, envanterId);

        assertThat(arizalar.post(ArizaSayimFixture.ariza().ile("envanterId", envanterId)))
            .hasStatus(422).bodyJson().extractingPath("$.alan").isEqualTo("envanterId");
    }

    @Test
    void shouldApplyRowTypeRulesForLocalAndGeneralRows() {
        long sayimId = sayimlar.olustur(ArizaSayimFixture.sayim("YEREL"));
        ApiIstemcisi satir = satirlar(sayimId);

        assertThat(satir.post(ArizaSayimFixture.yerelSatir("ZIMMET").ile("islem", "DUZELTME")))
            .hasStatus(422).bodyJson().extractingPath("$.detail")
            .isEqualTo("DUZELTME işlemi YEREL sayım satırında kullanılamaz.");
        assertThat(satir.post(ArizaSayimFixture.yerelSatir("SARF").olmadan("zimmetEskiRef"))).hasStatus(422);
        assertThat(satir.post(ArizaSayimFixture.genelSatir().olmadan("islemTarihi"))).hasStatus(422);

        long envanterId = veri.demirbasEnvanter(e -> { });
        assertThat(satir.post(ArizaSayimFixture.yerelSatir("SARF").ile("envanterId", envanterId)))
            .hasStatus(422).bodyJson().extractingPath("$.kod").isEqualTo("IS_KURALI_IHLALI");

        long yerelId = satir.olustur(ArizaSayimFixture.yerelSatir("ZIMMET").ile("envanterId", envanterId));
        long genelId = satir.olustur(ArizaSayimFixture.genelSatir());
        assertThat(satir.get(genelId)).hasStatusOk().bodyJson()
            .hasPathSatisfying("$.anlikMarka", m -> assertThat(m).asString().isEqualTo("Dell"))
            .hasPathSatisfying("$.malzemeTuru", m -> assertThat(m).isNull());

        // ZIMMET → SARF: envanter bağlantısı kaldırılarak güncellenebilir
        assertThat(satir.put(yerelId, ArizaSayimFixture.yerelSatir("SARF").guncelleme(0)))
            .hasStatusOk().bodyJson().hasPathSatisfying("$.envanter", e -> assertThat(e).isNull());
    }

    @Test
    void shouldDeleteRowsWithSayimAndRejectRowOfAnotherSayim() {
        long sayimId = sayimlar.olustur(ArizaSayimFixture.sayim("GENEL"));
        long baskaSayimId = sayimlar.olustur(ArizaSayimFixture.sayim("GENEL"));
        long satirId = satirlar(sayimId).olustur(ArizaSayimFixture.genelSatir());

        assertThat(satirlar(baskaSayimId).get(satirId)).hasStatus(404);
        assertThat(satirlar(baskaSayimId).delete(satirId)).hasStatus(404);

        assertThat(sayimlar.delete(sayimId)).hasStatus(204);
        assertThat(silinmisMi("SAYIM_SATIRI", satirId)).isTrue();
        assertThat(sayimlar.get(sayimId)).hasStatus(404);
        assertThat(satirlar(sayimId).post(ArizaSayimFixture.genelSatir())).hasStatus(404);
    }
}
