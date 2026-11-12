package tr.depo.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.transaction.PlatformTransactionManager;
import tr.depo.backend.util.ApiIstemcisi;
import tr.depo.backend.util.EnvanterFixture;
import tr.depo.backend.util.OracleEntegrasyonTestTabani;
import tr.depo.backend.util.TanimSeti;
import tr.depo.backend.util.VeritabaniFixture;
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
    /** Ürün oluştururken zorunlu kategori, marka ve model. */
    private TanimSeti demirbasTanimlari;
    private TanimSeti sarfTanimlari;

    @BeforeEach
    void hazirla() {
        veri = new VeritabaniFixture(entityManager, islemYoneticisi);
        jdbc = new JdbcTemplate(veriKaynagi);
        demirbaslar = new ApiIstemcisi(mvc, "/api/v1/demirbaslar");
        sarflar = new ApiIstemcisi(mvc, "/api/v1/sarf-malzemeleri");
        demirbasTanimlari = veri.demirbasTanimlari("Bilgisayar");
        sarfTanimlari = veri.sarfTanimlari("Toner");
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
        long modelId = veri.demirbasModeli(markaId, "Latitude");
        long id = demirbaslar.olustur(EnvanterFixture.demirbas(demirbasTanimlari).ile("depoId", depoId)
            .ile("markaId", markaId).ile("modelId", modelId));

        assertThat(demirbaslar.get(id)).hasStatusOk().bodyJson()
            .hasPathSatisfying("$.marka.ad", m -> assertThat(m).asString().isEqualTo("Dell"))
            .hasPathSatisfying("$.depo.ad", d -> assertThat(d).asString().isEqualTo("Bilgi İşlem Deposu"))
            .hasPathSatisfying("$.zimmetliSicil", s -> assertThat(s).asString().isEqualTo("01234"));
        assertThat(demirbaslar.listedekiIdler()).contains((int) id);
        assertThat(sarflar.listedekiIdler()).doesNotContain((int) id);

        assertThat(sarflar.get(id)).hasStatus(404);
        assertThat(sarflar.put(id, EnvanterFixture.sarf("Toner", sarfTanimlari).guncelleme(0))).hasStatus(404);
        assertThat(sarflar.delete(id)).hasStatus(404);
        assertThat(demirbaslar.get(id)).hasStatusOk();
    }

    @Test
    void shouldRejectSarfDefinitionOnDemirbasAndInvalidWarranty() {
        long sarfMarka = veri.marka(MalzemeTuru.SARF, "Kırtasiye Markası");

        assertThat(demirbaslar.post(EnvanterFixture.demirbas(demirbasTanimlari).ile("markaId", sarfMarka)))
            .hasStatus(422).bodyJson().extractingPath("$.alan").isEqualTo("markaId");
        assertThat(demirbaslar.post(EnvanterFixture.demirbas(demirbasTanimlari).ile("garantiBitis", "2025-01-01T00:00:00")))
            .hasStatus(422).bodyJson().extractingPath("$.detail")
            .isEqualTo("Garanti bitiş tarihi başlangıç tarihinden önce olamaz.");
    }

    @Test
    void shouldRequireCategoryBrandAndModelAndAllowOnlyTheModelsOwnBrand() {
        long dell = veri.marka(MalzemeTuru.DEMIRBAS, "Dell Bilgisayar");
        long hp = veri.marka(MalzemeTuru.DEMIRBAS, "HP Bilgisayar");
        long latitude = veri.demirbasModeli(dell, "Latitude 5440");

        assertThat(demirbaslar.post(EnvanterFixture.demirbas(demirbasTanimlari).ile("markaId", hp).ile("modelId", latitude)))
            .hasStatus(422).bodyJson()
            .hasPathSatisfying("$.alan", a -> assertThat(a).asString().isEqualTo("modelId"))
            .hasPathSatisfying("$.detail", d -> assertThat(d).asString()
                .isEqualTo("Seçilen model bu markaya ait değil; yalnızca modelin kendi markası seçilebilir."));

        assertThat(demirbaslar.post(EnvanterFixture.demirbas(demirbasTanimlari)
                .olmadan("kategoriId").olmadan("markaId").olmadan("modelId")))
            .hasStatus(400).bodyJson().hasPathSatisfying("$.hatalar[*].alan",
                alanlar -> assertThat(alanlar).asArray().containsExactlyInAnyOrder("kategoriId", "markaId", "modelId"));
        assertThat(sarflar.post(EnvanterFixture.sarf("Toner", sarfTanimlari).olmadan("markaId")))
            .hasStatus(400).bodyJson().extractingPath("$.hatalar[0].alan").isEqualTo("markaId");

        assertThat(demirbaslar.post(EnvanterFixture.demirbas(demirbasTanimlari).ile("markaId", dell).ile("modelId", latitude)))
            .hasStatus(201);
    }

    @Test
    void shouldNotClearFilledDefinitionsOnUpdateButKeepLegacyEmptyOnesEditable() {
        long id = demirbaslar.olustur(EnvanterFixture.demirbas(demirbasTanimlari));

        assertThat(demirbaslar.put(id, EnvanterFixture.demirbas(demirbasTanimlari).olmadan("kategoriId").guncelleme(0)))
            .hasStatus(422).bodyJson().extractingPath("$.detail")
            .isEqualTo("'kategoriId' boşaltılamaz: kayıtta dolu olan kategori, marka ve model güncellemede de gönderilmelidir.");
        assertThat(demirbaslar.get(id)).as("geri alındı").hasStatusOk().bodyJson()
            .hasPathSatisfying("$.kategori.id", k -> assertThat(k).asNumber().isEqualTo((int) demirbasTanimlari.kategoriId()))
            .hasPathSatisfying("$.versiyon", v -> assertThat(v).asNumber().isEqualTo(0));

        assertThat(demirbaslar.put(id, EnvanterFixture.demirbas(demirbasTanimlari).olmadan("markaId").guncelleme(0)))
            .as("marka boş ama modelden dolduruluyor: boşaltma sayılmaz").hasStatusOk().bodyJson()
            .hasPathSatisfying("$.marka.id", m -> assertThat(m).asNumber().isEqualTo((int) demirbasTanimlari.markaId()));

        long sarf = sarflar.olustur(EnvanterFixture.sarf("Toner", sarfTanimlari));
        assertThat(sarflar.put(sarf, EnvanterFixture.sarf("Toner", sarfTanimlari).olmadan("modelId").guncelleme(0)))
            .hasStatus(422);

        long legacy = veri.demirbasEnvanter(e -> { });
        assertThat(demirbaslar.put(legacy, EnvanterFixture.demirbas(demirbasTanimlari)
                .olmadan("kategoriId").olmadan("markaId").olmadan("modelId").guncelleme(0)))
            .as("legacy'den boş gelen tanımlar boş kalabilir").hasStatusOk();
    }

    @Test
    void shouldKeepDemirbasNoAndSerialNoUniqueAmongActiveDemirbaslar() {
        long id = demirbaslar.olustur(EnvanterFixture.demirbas(demirbasTanimlari).ile("demirbasNo", 900_001).ile("seriNo", "SN-T-1"));

        assertThat(demirbaslar.post(EnvanterFixture.demirbas(demirbasTanimlari).ile("demirbasNo", 900_001)))
            .hasStatus(409).bodyJson()
            .hasPathSatisfying("$.kod", k -> assertThat(k).asString().isEqualTo("TEKILLIK_IHLALI"))
            .hasPathSatisfying("$.detail", d -> assertThat(d).asString()
                .isEqualTo("Bu demirbaş numarası başka bir demirbaşta kullanılıyor."));
        assertThat(demirbaslar.post(EnvanterFixture.demirbas(demirbasTanimlari).ile("seriNo", "SN-T-1")))
            .hasStatus(409).bodyJson().extractingPath("$.detail")
            .isEqualTo("Bu seri numarası başka bir demirbaşta kullanılıyor.");

        assertThat(demirbaslar.put(id, EnvanterFixture.demirbas(demirbasTanimlari).ile("demirbasNo", 900_001).ile("seriNo", "SN-T-1")
            .ile("ad", "Yeni ad").guncelleme(0))).as("kendi numarasıyla güncellenebilir").hasStatusOk();

        assertThat(demirbaslar.delete(id)).hasStatus(204);
        assertThat(demirbaslar.post(EnvanterFixture.demirbas(demirbasTanimlari).ile("demirbasNo", 900_001).ile("seriNo", "SN-T-1")))
            .as("silinmiş kaydın numarası yeniden kullanılabilir").hasStatus(201);
    }

    @Test
    void shouldStillUpdateLegacyDuplicateWhenItsNumberIsUnchanged() {
        long eski = veri.demirbasEnvanter(e -> e.setDemirbasNo(900_100L));
        veri.demirbasEnvanter(e -> e.setDemirbasNo(900_100L));

        assertThat(demirbaslar.put(eski, EnvanterFixture.demirbas(demirbasTanimlari).ile("demirbasNo", 900_100).guncelleme(0)))
            .hasStatusOk();
    }

    @Test
    void shouldListOnlyActiveDemirbaslarCurrentlyAssignedToTheGivenSicil() {
        long birinci = demirbaslar.olustur(EnvanterFixture.demirbas(demirbasTanimlari).ile("zimmetliSicil", "7700001"));
        long ikinci = demirbaslar.olustur(EnvanterFixture.demirbas(demirbasTanimlari).ile("zimmetliSicil", "7700001"));
        long silinen = demirbaslar.olustur(EnvanterFixture.demirbas(demirbasTanimlari).ile("zimmetliSicil", "7700001"));
        long baskasinin = demirbaslar.olustur(EnvanterFixture.demirbas(demirbasTanimlari).ile("zimmetliSicil", "7700002"));
        long sarf = sarflar.olustur(EnvanterFixture.sarf("Toner", sarfTanimlari));
        assertThat(demirbaslar.delete(silinen)).hasStatus(204);

        MvcTestResult sonuc = demirbaslar.listele("zimmetliSicil", " 7700001 ");
        assertThat(sonuc).hasStatusOk();
        List<Integer> idler = JsonPath.read(ApiIstemcisi.govde(sonuc), "$.icerik[*].id");
        assertThat(idler).containsExactlyInAnyOrder((int) birinci, (int) ikinci)
            .doesNotContain((int) silinen, (int) baskasinin, (int) sarf);

        assertThat(demirbaslar.listele("zimmetliSicil", "0000000")).hasStatusOk().bodyJson()
            .extractingPath("$.toplamEleman").isEqualTo(0);
        assertThat(demirbaslar.listele("zimmetliSicil", " ")).hasStatus(400).bodyJson()
            .extractingPath("$.kod").isEqualTo("GECERSIZ_ISTEK");
    }

    @Test
    void shouldRecordEverySicilChangeOfADemirbasAndListNewestFirst() {
        long id = demirbaslar.olustur(EnvanterFixture.demirbas(demirbasTanimlari).ile("zimmetliSicil", "8800001"));
        assertThat(demirbaslar.put(id, EnvanterFixture.demirbas(demirbasTanimlari).ile("zimmetliSicil", "8800002").guncelleme(0)))
            .hasStatusOk();
        assertThat(demirbaslar.put(id, EnvanterFixture.demirbas(demirbasTanimlari).ile("zimmetliSicil", "8800002").guncelleme(1)))
            .as("sicil değişmedi: geçmişe yazılmaz").hasStatusOk();
        assertThat(demirbaslar.put(id, EnvanterFixture.demirbas(demirbasTanimlari).olmadan("zimmetliSicil").guncelleme(2)))
            .as("zimmet kaldırıldı").hasStatusOk();

        MvcTestResult gecmis = mvc.get().uri("/api/v1/demirbaslar/{id}/zimmet-gecmisi", id).exchange();
        assertThat(gecmis).hasStatusOk().bodyJson()
            .hasPathSatisfying("$.toplamEleman", t -> assertThat(t).asNumber().isEqualTo(3))
            .hasPathSatisfying("$.icerik[0].oncekiSicil", s -> assertThat(s).asString().isEqualTo("8800002"))
            .hasPathSatisfying("$.icerik[0].yeniSicil", s -> assertThat(s).isNull())
            .hasPathSatisfying("$.icerik[1].oncekiSicil", s -> assertThat(s).asString().isEqualTo("8800001"))
            .hasPathSatisfying("$.icerik[1].yeniSicil", s -> assertThat(s).asString().isEqualTo("8800002"))
            .hasPathSatisfying("$.icerik[2].oncekiSicil", s -> assertThat(s).isNull())
            .hasPathSatisfying("$.icerik[2].yeniSicil", s -> assertThat(s).asString().isEqualTo("8800001"))
            .hasPathSatisfying("$.icerik[2].kaynak", k -> assertThat(k).asString().isEqualTo("DEGISIKLIK"))
            .hasPathSatisfying("$.icerik[2].demirbasId", d -> assertThat(d).asNumber().isEqualTo((int) id))
            .hasPathSatisfying("$.icerik[2].olusturan", o -> assertThat(o).asString().isEqualTo("API"));

        long sarf = sarflar.olustur(EnvanterFixture.sarf("Toner", sarfTanimlari));
        assertThat(mvc.get().uri("/api/v1/demirbaslar/{id}/zimmet-gecmisi", sarf)).hasStatus(404);
        assertThat(demirbaslar.delete(id)).hasStatus(204);
        assertThat(mvc.get().uri("/api/v1/demirbaslar/{id}/zimmet-gecmisi", id)).hasStatus(404);
    }

    @Test
    void shouldCreateUpdateAndDeleteSarfMalzemesi() {
        long id = sarflar.olustur(EnvanterFixture.sarf("Toner 85A", sarfTanimlari));

        assertThat(sarflar.put(id, EnvanterFixture.sarf("Toner 85A XL", sarfTanimlari).guncelleme(0)))
            .hasStatusOk().bodyJson()
            .hasPathSatisfying("$.ad", a -> assertThat(a).asString().isEqualTo("Toner 85A XL"))
            .hasPathSatisfying("$.versiyon", v -> assertThat(v).asNumber().isEqualTo(1));
        assertThat(demirbaslar.get(id)).hasStatus(404);
        assertThat(sarflar.delete(id)).hasStatus(204);
        assertThat(silinmisMi("ENVANTER", id)).isTrue();
    }

    @Test
    void shouldManageChildrenOnlyThroughTheirEnvanter() {
        long envanterId = demirbaslar.olustur(EnvanterFixture.demirbas(demirbasTanimlari));
        long baskaEnvanterId = demirbaslar.olustur(EnvanterFixture.demirbas(demirbasTanimlari));
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
        long envanterId = demirbaslar.olustur(EnvanterFixture.demirbas(demirbasTanimlari));
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
        long envanterId = demirbaslar.olustur(EnvanterFixture.demirbas(demirbasTanimlari));

        assertThat(altKaynak(envanterId, "aparatlar").post(EnvanterFixture.aparat("Kablo", 0))).hasStatus(400);
        long notId = altKaynak(envanterId, "notlar").olustur(EnvanterFixture.not("Not").ile("ekleyenSicil", " 0099 "));
        assertThat(altKaynak(envanterId, "notlar").get(notId)).hasStatusOk()
            .bodyJson().extractingPath("$.ekleyenSicil").isEqualTo("0099");
    }
}
