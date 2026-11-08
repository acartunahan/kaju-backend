package tr.depo.backend.marka;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.transaction.PlatformTransactionManager;
import tr.depo.backend.support.OracleEntegrasyonTestTabani;
import tr.depo.backend.support.fixture.MarkaFixture;
import tr.depo.backend.support.fixture.VeritabaniFixture;
import tr.depo.domain.tanim.Depo;
import tr.depo.domain.tanim.Model;

/** Marka API'si uçtan uca, gerçek Oracle'da (Flyway şeması, soft delete trigger'ları, Hibernate filtresi). */
class MarkaEntegrasyonTesti extends OracleEntegrasyonTestTabani {

    private static final String YOL = "/api/v1/markalar";

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

    @BeforeEach
    void hazirla() {
        veri = new VeritabaniFixture(entityManager, islemYoneticisi);
        jdbc = new JdbcTemplate(veriKaynagi);
    }

    private MvcTestResult post(MarkaFixture marka) {
        return mvc.post().uri(YOL).contentType(MediaType.APPLICATION_JSON).content(marka.json()).exchange();
    }

    private MvcTestResult put(long id, MarkaFixture marka) {
        return mvc.put().uri(YOL + "/" + id).contentType(MediaType.APPLICATION_JSON).content(marka.guncellemeJson()).exchange();
    }

    private long olustur(MarkaFixture marka) {
        MvcTestResult sonuc = post(marka);
        assertThat(sonuc).hasStatus(201);
        return ((Number) JsonPath.read(govde(sonuc), "$.id")).longValue();
    }

    private static String govde(MvcTestResult sonuc) {
        try {
            return sonuc.getResponse().getContentAsString();
        } catch (java.io.UnsupportedEncodingException hata) {
            throw new IllegalStateException(hata);
        }
    }

    private List<Integer> listedekiIdler(String sorgu) {
        MvcTestResult sonuc = mvc.get().uri(YOL + sorgu).exchange();
        assertThat(sonuc).hasStatusOk();
        return JsonPath.read(govde(sonuc), "$.icerik[*].id");
    }

    @Test
    void shouldCreateReadUpdateAndListMarkaWithAuditAndVersion() {
        long depoId = veri.depo("Merkez Depo");
        MvcTestResult olusan = post(MarkaFixture.sarfMarkasi().ad("HP").depoId(depoId));

        assertThat(olusan).hasStatus(201)
            .bodyJson()
            .hasPathSatisfying("$.versiyon", v -> assertThat(v).asNumber().isEqualTo(0))
            .hasPathSatisfying("$.olusturan", o -> assertThat(o).asString().isEqualTo("API"))
            .hasPathSatisfying("$.depo.ad", d -> assertThat(d).asString().isEqualTo("Merkez Depo"));
        long id = ((Number) JsonPath.read(govde(olusan), "$.id")).longValue();
        assertThat(olusan.getResponse().getHeader(HttpHeaders.LOCATION)).endsWith(YOL + "/" + id);

        assertThat(put(id, MarkaFixture.sarfMarkasi().ad("HP Inc.").versiyon(0).depoId(depoId)))
            .hasStatusOk()
            .bodyJson()
            .hasPathSatisfying("$.ad", a -> assertThat(a).asString().isEqualTo("HP Inc."))
            .hasPathSatisfying("$.versiyon", v -> assertThat(v).asNumber().isEqualTo(1))
            .hasPathSatisfying("$.guncelleyen", g -> assertThat(g).asString().isEqualTo("API"));

        assertThat(mvc.get().uri(YOL + "/" + id)).hasStatusOk()
            .bodyJson().extractingPath("$.ad").isEqualTo("HP Inc.");
        assertThat(listedekiIdler("?size=100&sort=id,desc")).contains((int) id);
    }

    @Test
    void shouldSoftDeleteMarkaAndKeepRowInDatabase() {
        long id = olustur(MarkaFixture.demirbasMarkasi().ad("Silinecek"));

        assertThat(mvc.delete().uri(YOL + "/" + id)).hasStatus(204);

        Map<String, Object> satir = jdbc.queryForMap("SELECT SILINME_ZAMANI, SILEN FROM MARKA WHERE ID = ?", id);
        assertThat(satir.get("SILINME_ZAMANI")).as("satır fiziksel olarak silinmedi, işaretlendi").isNotNull();
        assertThat(satir).containsEntry("SILEN", "API");
    }

    @Test
    void shouldReturn404OnGetPutAndDeleteWhenMarkaIsSoftDeletedAndHideItFromList() {
        long id = olustur(MarkaFixture.demirbasMarkasi().ad("Gizlenecek"));
        assertThat(mvc.delete().uri(YOL + "/" + id)).hasStatus(204);

        assertThat(mvc.get().uri(YOL + "/" + id)).hasStatus(404)
            .bodyJson().extractingPath("$.kod").isEqualTo("KAYIT_BULUNAMADI");
        assertThat(put(id, MarkaFixture.demirbasMarkasi().versiyon(1))).hasStatus(404);
        assertThat(mvc.delete().uri(YOL + "/" + id)).hasStatus(404);
        assertThat(listedekiIdler("?size=100&sort=id,desc")).doesNotContain((int) id);
    }

    @Test
    void shouldReturn409WhenTwoUpdatesUseSameVersion() {
        long id = olustur(MarkaFixture.demirbasMarkasi().ad("Çakışma"));

        assertThat(put(id, MarkaFixture.demirbasMarkasi().ad("Birinci").versiyon(0))).hasStatusOk();
        assertThat(put(id, MarkaFixture.demirbasMarkasi().ad("İkinci").versiyon(0)))
            .hasStatus(409)
            .bodyJson().extractingPath("$.kod").isEqualTo("SURUM_CAKISMASI");
        assertThat(mvc.get().uri(YOL + "/" + id)).bodyJson().extractingPath("$.ad").isEqualTo("Birinci");
    }

    @Test
    void shouldReturn409WhenActiveModelUsesMarkaAndAllowDeleteAfterModelIsDeleted() {
        long markaId = olustur(MarkaFixture.sarfMarkasi().ad("Kullanılan"));
        long modelId = veri.sarfModeli(markaId, "Toner 85A");

        assertThat(mvc.delete().uri(YOL + "/" + markaId))
            .hasStatus(409)
            .bodyJson().hasPathSatisfying("$.detail", d -> assertThat(d).asString().contains("1 aktif model"));

        veri.sil(Model.class, modelId);
        assertThat(mvc.delete().uri(YOL + "/" + markaId)).hasStatus(204);
    }

    @Test
    void shouldReturn422WhenReferencedDepoIsSoftDeleted() {
        long depoId = veri.depo("Kapanan Depo");
        veri.sil(Depo.class, depoId);

        assertThat(post(MarkaFixture.demirbasMarkasi().depoId(depoId)))
            .hasStatus(422)
            .bodyJson()
            .hasPathSatisfying("$.kod", k -> assertThat(k).asString().isEqualTo("REFERANS_GECERSIZ"))
            .hasPathSatisfying("$.alan", a -> assertThat(a).asString().isEqualTo("depoId"));
        assertThat(post(MarkaFixture.demirbasMarkasi().depoId(987_654_321L))).hasStatus(422);
    }

    @Test
    void shouldHideSoftDeletedDepoInResponseButStillListMarka() {
        long depoId = veri.depo("Sonradan Silinen Depo");
        long markaId = olustur(MarkaFixture.demirbasMarkasi().ad("Depolu").depoId(depoId));
        veri.sil(Depo.class, depoId);

        assertThat(mvc.get().uri(YOL + "/" + markaId)).hasStatusOk()
            .bodyJson().hasPathSatisfying("$.depo", d -> assertThat(d).isNull());
        assertThat(listedekiIdler("?size=100&sort=id,desc")).contains((int) markaId);
    }

    @Test
    void shouldReturn422WhenClearingActiveFlagOfSarfMarka() {
        long id = olustur(MarkaFixture.sarfMarkasi().ad("Sarf"));

        assertThat(put(id, MarkaFixture.sarfMarkasi().aktif(null).versiyon(0)))
            .hasStatus(422)
            .bodyJson().extractingPath("$.detail").isEqualTo("Sarf markasında “aktif” alanı boş bırakılamaz.");
    }

    @Test
    void shouldCapPageSizeAndRejectUnknownSortField() {
        assertThat(mvc.get().uri(YOL + "?size=1000")).hasStatusOk()
            .bodyJson().extractingPath("$.boyut").isEqualTo(100);
        assertThat(mvc.get().uri(YOL + "?sort=silen,asc")).hasStatus(400)
            .bodyJson().extractingPath("$.kod").isEqualTo("GECERSIZ_SIRALAMA");
    }
}
