package tr.depo.backend.tanim;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.transaction.PlatformTransactionManager;
import tr.depo.backend.support.ApiIstemcisi;
import tr.depo.backend.support.OracleEntegrasyonTestTabani;
import tr.depo.backend.support.fixture.TanimFixture;
import tr.depo.backend.support.fixture.VeritabaniFixture;
import tr.depo.domain.envanter.Envanter;
import tr.depo.domain.tanim.Depo;
import tr.depo.domain.tanim.Firma;
import tr.depo.domain.tanim.Ihale;
import tr.depo.domain.tanim.MalzemeTuru;
import tr.depo.domain.tanim.Marka;
import tr.depo.domain.tanim.Model;

/** Tanım kaynakları (depo, firma, kategori, model, ihale) gerçek Oracle'da: kaynağa özgü kurallar ve soft delete. */
class TanimEntegrasyonTesti extends OracleEntegrasyonTestTabani {

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
    private ApiIstemcisi depolar;
    private ApiIstemcisi firmalar;
    private ApiIstemcisi kategoriler;
    private ApiIstemcisi modeller;
    private ApiIstemcisi ihaleler;

    @BeforeEach
    void hazirla() {
        veri = new VeritabaniFixture(entityManager, islemYoneticisi);
        jdbc = new JdbcTemplate(veriKaynagi);
        depolar = new ApiIstemcisi(mvc, "/api/v1/depolar");
        firmalar = new ApiIstemcisi(mvc, "/api/v1/firmalar");
        kategoriler = new ApiIstemcisi(mvc, "/api/v1/kategoriler");
        modeller = new ApiIstemcisi(mvc, "/api/v1/modeller");
        ihaleler = new ApiIstemcisi(mvc, "/api/v1/ihaleler");
    }

    // ------------------------------------------------------------------ Depo

    @Test
    void shouldCreateUpdateAndSoftDeleteDepo() {
        long id = depolar.olustur(TanimFixture.depo().ile("sorumluSicil", " 0001234 "));
        assertThat(depolar.get(id)).hasStatusOk().bodyJson().extractingPath("$.sorumluSicil").isEqualTo("0001234");

        assertThat(depolar.put(id, TanimFixture.depo().ile("ad", "Yeni Ad").olmadan("aciklama").guncelleme(0)))
            .hasStatusOk()
            .bodyJson().hasPathSatisfying("$.aciklama", a -> assertThat(a).isNull());

        assertThat(depolar.delete(id)).hasStatus(204);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM DEPO WHERE ID = ? AND SILINME_ZAMANI IS NOT NULL",
            Long.class, id)).isEqualTo(1);
        assertThat(depolar.get(id)).hasStatus(404);
        assertThat(depolar.listedekiIdler()).doesNotContain((int) id);
    }

    @Test
    void shouldBlockDepoDeleteWhileActiveRecordsUseItAndAllowAfterTheyAreDeleted() {
        long depoId = depolar.olustur(TanimFixture.depo().ile("ad", "Dolu Depo"));
        long envanterId = veri.demirbasEnvanter(e -> e.setDepo(veri.bul(Depo.class, depoId)));

        assertThat(depolar.delete(depoId)).hasStatus(409)
            .bodyJson().hasPathSatisfying("$.detail", d -> assertThat(d).asString().contains("1 aktif envanter"));

        veri.sil(Envanter.class, envanterId);
        assertThat(depolar.delete(depoId)).hasStatus(204);
    }

    // ------------------------------------------------------------------ Firma

    @Test
    void shouldEnforceTypeSpecificRequiredFieldsForFirma() {
        assertThat(firmalar.post(TanimFixture.demirbasFirmasi().olmadan("kayitTarihi"))).hasStatus(422)
            .bodyJson().extractingPath("$.detail").isEqualTo("Demirbaş firmasında “durumKodu” ve “kayitTarihi” zorunludur.");
        assertThat(firmalar.post(TanimFixture.sarfFirmasi().olmadan("ad"))).hasStatus(422);

        long id = firmalar.olustur(TanimFixture.demirbasFirmasi());
        assertThat(firmalar.get(id)).hasStatusOk().bodyJson()
            .hasPathSatisfying("$.yetkiliAd", a -> assertThat(a).asString().isEqualTo("Ayşe Yılmaz"))
            .hasPathSatisfying("$.irtibatAd", a -> assertThat(a).isNull());
        assertThat(firmalar.put(id, TanimFixture.demirbasFirmasi().olmadan("durumKodu").guncelleme(0))).hasStatus(422);
    }

    @Test
    void shouldBlockFirmaDeleteWhileActiveIhaleUsesIt() {
        long firmaId = firmalar.olustur(TanimFixture.demirbasFirmasi());
        long ihaleId = ihaleler.olustur(TanimFixture.ihale().ile("firmaId", firmaId));

        assertThat(firmalar.delete(firmaId)).hasStatus(409);
        assertThat(ihaleler.delete(ihaleId)).hasStatus(204);
        assertThat(firmalar.delete(firmaId)).hasStatus(204);
    }

    // ------------------------------------------------------------------ Kategori

    @Test
    void shouldBuildCategoryTreeAndRejectCycles() {
        long kok = kategoriler.olustur(TanimFixture.demirbasKategorisi("Elektronik"));
        long alt = kategoriler.olustur(TanimFixture.demirbasKategorisi("Bilgisayar").ile("ustKategoriId", kok));
        long torun = kategoriler.olustur(TanimFixture.demirbasKategorisi("Dizüstü").ile("ustKategoriId", alt));

        assertThat(kategoriler.get(torun)).hasStatusOk().bodyJson().extractingPath("$.ust.ad").isEqualTo("Bilgisayar");
        assertThat(kategoriler.put(kok, TanimFixture.demirbasKategorisi("Elektronik").ile("ustKategoriId", torun).guncelleme(0)))
            .hasStatus(422)
            .bodyJson().extractingPath("$.kod").isEqualTo("IS_KURALI_IHLALI");
        assertThat(kategoriler.put(kok, TanimFixture.demirbasKategorisi("Elektronik").ile("ustKategoriId", kok).guncelleme(0)))
            .hasStatus(422);
    }

    @Test
    void shouldRejectParentCategoryOfOtherTypeAndMissingStatusCode() {
        long demirbasKok = kategoriler.olustur(TanimFixture.demirbasKategorisi("Mobilya"));

        assertThat(kategoriler.post(TanimFixture.sarfKategorisi("Kâğıt").ile("ustKategoriId", demirbasKok)))
            .hasStatus(422)
            .bodyJson()
            .hasPathSatisfying("$.alan", a -> assertThat(a).asString().isEqualTo("ustKategoriId"))
            .hasPathSatisfying("$.detail", d -> assertThat(d).asString().contains("SARF"));
        assertThat(kategoriler.post(TanimFixture.demirbasKategorisi("Masa").olmadan("durumKodu"))).hasStatus(422);
    }

    @Test
    void shouldBlockCategoryDeleteWhileActiveSubCategoryExists() {
        long kok = kategoriler.olustur(TanimFixture.sarfKategorisi("Kırtasiye"));
        long alt = kategoriler.olustur(TanimFixture.sarfKategorisi("Kalem").ile("ustKategoriId", kok));

        assertThat(kategoriler.delete(kok)).hasStatus(409)
            .bodyJson().hasPathSatisfying("$.detail", d -> assertThat(d).asString().contains("alt kategori"));
        assertThat(kategoriler.delete(alt)).hasStatus(204);
        assertThat(kategoriler.delete(kok)).hasStatus(204);
    }

    // ------------------------------------------------------------------ Model

    @Test
    void shouldCreateSarfModelOnlyWithSarfBrand() {
        long sarfMarka = veri.marka(MalzemeTuru.SARF, "Canon");
        long demirbasMarka = veri.marka(MalzemeTuru.DEMIRBAS, "Dell");

        assertThat(modeller.post(TanimFixture.sarfModeli("Toner", demirbasMarka)))
            .hasStatus(422).bodyJson().extractingPath("$.alan").isEqualTo("markaId");
        assertThat(modeller.post(TanimFixture.sarfModeli("Toner", sarfMarka).olmadan("markaId")))
            .hasStatus(422).bodyJson().extractingPath("$.detail").isEqualTo("Sarf modelinde “markaId” zorunludur.");

        long aktifsiz = modeller.olustur(TanimFixture.sarfModeli("Toner 85A", sarfMarka).olmadan("aktif"));
        assertThat(modeller.get(aktifsiz)).as("aktif boşsa true").hasStatusOk().bodyJson()
            .extractingPath("$.aktif").isEqualTo(true);
        assertThat(modeller.put(aktifsiz, TanimFixture.sarfModeli("Toner 85A", sarfMarka).olmadan("aktif").guncelleme(0)))
            .as("PUT'ta aktif boşaltılamaz").hasStatus(422);

        long id = modeller.olustur(TanimFixture.sarfModeli("Toner 725", sarfMarka));
        assertThat(modeller.get(id)).hasStatusOk().bodyJson().extractingPath("$.marka.ad").isEqualTo("Canon");
    }

    @Test
    void shouldBlockModelDeleteWhileActiveEnvanterUsesItAndHideDeletedBrand() {
        long markaId = veri.marka(MalzemeTuru.DEMIRBAS, "Lenovo");
        long modelId = modeller.olustur(TanimFixture.demirbasModeli("ThinkPad").ile("markaId", markaId));
        long envanterId = veri.demirbasEnvanter(e -> e.setModel(veri.bul(Model.class, modelId)));

        assertThat(modeller.delete(modelId)).hasStatus(409);
        veri.sil(Envanter.class, envanterId);
        veri.sil(Marka.class, markaId);
        assertThat(modeller.get(modelId)).hasStatusOk().bodyJson().hasPathSatisfying("$.marka", m -> assertThat(m).isNull());
        assertThat(modeller.delete(modelId)).hasStatus(204);
    }

    // ------------------------------------------------------------------ İhale

    @Test
    void shouldLinkIhaleOnlyToActiveDemirbasFirma() {
        long sarfFirma = firmalar.olustur(TanimFixture.sarfFirmasi());
        long silinecekFirma = firmalar.olustur(TanimFixture.demirbasFirmasi());
        firmalar.delete(silinecekFirma);

        assertThat(ihaleler.post(TanimFixture.ihale().ile("firmaId", sarfFirma))).hasStatus(422);
        assertThat(ihaleler.post(TanimFixture.ihale().ile("firmaId", silinecekFirma))).hasStatus(422)
            .bodyJson().extractingPath("$.kod").isEqualTo("REFERANS_GECERSIZ");

        long id = ihaleler.olustur(TanimFixture.ihale());
        assertThat(ihaleler.get(id)).hasStatusOk().bodyJson()
            .hasPathSatisfying("$.komisyonBaskani", b -> assertThat(b).asString().isEqualTo("Başkan"))
            .hasPathSatisfying("$.komisyonUye2", u -> assertThat(u).isNull());
    }

    @Test
    void shouldBlockIhaleDeleteWhileActiveEnvanterUsesIt() {
        long ihaleId = ihaleler.olustur(TanimFixture.ihale());
        long envanterId = veri.demirbasEnvanter(e -> e.setIhale(veri.bul(Ihale.class, ihaleId)));

        assertThat(ihaleler.delete(ihaleId)).hasStatus(409);
        veri.sil(Envanter.class, envanterId);
        assertThat(ihaleler.delete(ihaleId)).hasStatus(204);
        Map<String, Object> satir = jdbc.queryForMap("SELECT SILEN FROM IHALE WHERE ID = ?", ihaleId);
        assertThat(satir).containsEntry("SILEN", "API");
        assertThat(veri.oku(Firma.class, firmalar.olustur(TanimFixture.sarfFirmasi()), Firma::getTur))
            .isEqualTo(MalzemeTuru.SARF);
    }
}
