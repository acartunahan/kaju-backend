package tr.depo.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.transaction.PlatformTransactionManager;
import tr.depo.backend.util.ApiIstemcisi;
import tr.depo.backend.util.GecmisKayitFixture;
import tr.depo.backend.util.OracleEntegrasyonTestTabani;
import tr.depo.backend.util.VeritabaniFixture;
import tr.depo.domain.envanter.Envanter;
import tr.depo.domain.tanim.Depo;

/** Geçmiş kayıtları gerçek Oracle'da: tür uyumu, değiştirilemezlik ve silinmiş envanterin geçmişinin korunması. */
class GecmisKayitEntegrasyonTesti extends OracleEntegrasyonTestTabani {

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
    private ApiIstemcisi hareketler;
    private ApiIstemcisi zimmetKayitlari;

    @BeforeEach
    void hazirla() {
        veri = new VeritabaniFixture(entityManager, islemYoneticisi);
        jdbc = new JdbcTemplate(veriKaynagi);
        hareketler = new ApiIstemcisi(mvc, "/api/v1/envanter-hareketleri");
        zimmetKayitlari = new ApiIstemcisi(mvc, "/api/v1/zimmet-kayitlari");
    }

    @Test
    void shouldRecordDemirbasTransferWithDepotSummaries() {
        long envanterId = veri.demirbasEnvanter(e -> e.setAd("Projektör"));
        long depoId = veri.depo("Merkez");
        long id = hareketler.olustur(GecmisKayitFixture.demirbasHareketi(envanterId).ile("neredenDepoId", depoId));

        assertThat(hareketler.get(id)).hasStatusOk().bodyJson()
            .hasPathSatisfying("$.envanter.ad", a -> assertThat(a).asString().isEqualTo("Projektör"))
            .hasPathSatisfying("$.neredenDepo.ad", d -> assertThat(d).asString().isEqualTo("Merkez"))
            .hasPathSatisfying("$.nereyeMetni", n -> assertThat(n).asString().isEqualTo("Ankara Şube"));
        assertThat(hareketler.listedekiIdler()).contains((int) id);
    }

    @Test
    void shouldRejectMovementWhoseTypeDiffersFromEnvanterOrWhoseEnvanterIsDeleted() {
        long demirbasId = veri.demirbasEnvanter(e -> { });
        long silinenSarfId = veri.sarfEnvanter("Kağıt", e -> { });
        veri.sil(Envanter.class, silinenSarfId);

        assertThat(hareketler.post(GecmisKayitFixture.sarfHareketi(demirbasId)))
            .hasStatus(422).bodyJson()
            .hasPathSatisfying("$.alan", a -> assertThat(a).asString().isEqualTo("envanterId"))
            .hasPathSatisfying("$.detail", d -> assertThat(d).asString().contains("SARF"));
        assertThat(hareketler.post(GecmisKayitFixture.sarfHareketi(silinenSarfId))).hasStatus(422);
        assertThat(hareketler.post(GecmisKayitFixture.demirbasHareketi(demirbasId).olmadan("hareketNoKodu")))
            .hasStatus(422);
    }

    @Test
    void shouldKeepHistoryReadableAfterEnvanterIsDeletedAndNeverDeleteIt() {
        long sarfId = veri.sarfEnvanter("Toner", e -> { });
        long id = hareketler.olustur(GecmisKayitFixture.sarfHareketi(sarfId));
        veri.sil(Envanter.class, sarfId);

        assertThat(hareketler.get(id)).hasStatusOk().bodyJson()
            .hasPathSatisfying("$.miktar", m -> assertThat(m).asNumber().isEqualTo(12.5))
            .hasPathSatisfying("$.envanter", e -> assertThat(e).as("silinmiş envanter ilişkide görünmez").isNull());
        assertThat(hareketler.delete(id)).hasStatus(405);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM ENVANTER_HAREKETI WHERE ID = ? AND SILINME_ZAMANI IS NULL",
            Long.class, id)).isEqualTo(1);
    }

    @Test
    void shouldRecordZimmetAndRejectDeletedDepo() {
        long depoId = veri.depo("Kapanan Depo");
        veri.sil(Depo.class, depoId);

        assertThat(zimmetKayitlari.post(GecmisKayitFixture.zimmetKaydi().ile("depoId", depoId)))
            .hasStatus(422).bodyJson().extractingPath("$.alan").isEqualTo("depoId");

        long id = zimmetKayitlari.olustur(GecmisKayitFixture.zimmetKaydi());
        assertThat(zimmetKayitlari.get(id)).hasStatusOk().bodyJson()
            .hasPathSatisfying("$.islem", i -> assertThat(i).asString().isEqualTo("ZIMMET"))
            .hasPathSatisfying("$.malzemeEskiRef", m -> assertThat(m).isNull());
        assertThat(zimmetKayitlari.listedekiIdler()).contains((int) id);
    }
}
