package tr.depo.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.transaction.PlatformTransactionManager;
import tr.depo.backend.util.ApiIstemcisi;
import tr.depo.backend.util.ArizaSayimFixture;
import tr.depo.backend.util.EnvanterFixture;
import tr.depo.backend.util.GecmisKayitFixture;
import tr.depo.backend.util.OracleEntegrasyonTestTabani;
import tr.depo.backend.util.TanimFixture;
import tr.depo.backend.util.TanimSeti;
import tr.depo.backend.util.VeritabaniFixture;
import tr.depo.domain.tanim.MalzemeTuru;

/**
 * Liste filtreleri ve geçmiş uçları gerçek Oracle'da: kategori (alt kategoriler dahil), marka, model, depo, ihale,
 * durum, metin araması, barkodla bulma, tanım listesi filtreleri, kategori ağacı, ürün hareketleri ve arızaları.
 * Testler aynı veritabanını paylaştığı için sonuçlar bu testin oluşturduğu kayıtlarla sınırlanarak kontrol edilir.
 */
class ListelemeEntegrasyonTesti extends OracleEntegrasyonTestTabani {

    private static final AtomicInteger SAYAC = new AtomicInteger();

    @Autowired
    private MockMvcTester mvc;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private PlatformTransactionManager islemYoneticisi;

    private VeritabaniFixture veri;
    private ApiIstemcisi demirbaslar;
    private ApiIstemcisi sarflar;
    private ApiIstemcisi kategoriler;
    private String ek;

    @BeforeEach
    void hazirla() {
        veri = new VeritabaniFixture(entityManager, islemYoneticisi);
        demirbaslar = new ApiIstemcisi(mvc, "/api/v1/demirbaslar");
        sarflar = new ApiIstemcisi(mvc, "/api/v1/sarf-malzemeleri");
        kategoriler = new ApiIstemcisi(mvc, "/api/v1/kategoriler");
        ek = "L" + SAYAC.incrementAndGet() + "X" + System.nanoTime() % 100_000;
    }

    private List<Integer> idler(String yol, Map<String, String> parametreler) {
        var istek = mvc.get().uri(yol);
        parametreler.forEach(istek::param);
        MvcTestResult sonuc = istek.param("size", "100").exchange();
        assertThat(sonuc).as("GET %s %s", yol, parametreler).hasStatusOk();
        String govde = ApiIstemcisi.govde(sonuc);
        return JsonPath.read(govde, govde.startsWith("[") ? "$[*].id" : "$.icerik[*].id");
    }

    // ------------------------------------------------------------------ A: ürün filtreleri

    @Test
    void shouldFilterDemirbaslarByCategoryIncludingSubcategoriesAndByBrandModelDepotTenderAndStatus() {
        long ust = kategoriler.olustur(TanimFixture.demirbasKategorisi("Bilişim " + ek));
        long alt = kategoriler.olustur(TanimFixture.demirbasKategorisi("Dizüstü " + ek).ile("ustKategoriId", ust));
        TanimSeti tanim = veri.demirbasTanimlari("A" + ek);
        TanimSeti baskaMarka = veri.demirbasTanimlari("B" + ek);
        long depo = veri.depo("Depo " + ek);

        long ustteki = demirbaslar.olustur(EnvanterFixture.demirbas(tanim).ile("kategoriId", ust).ile("depoId", depo));
        long alttaki = demirbaslar.olustur(EnvanterFixture.demirbas(tanim).ile("kategoriId", alt).ile("durum", "ARIZALI"));
        long baskasi = demirbaslar.olustur(EnvanterFixture.demirbas(baskaMarka));

        assertThat(idler("/api/v1/demirbaslar", Map.of("kategoriId", "" + ust)))
            .as("alt kategoriler dahil").containsExactlyInAnyOrder((int) ustteki, (int) alttaki);
        assertThat(idler("/api/v1/demirbaslar", Map.of("kategoriId", "" + ust, "altKategoriler", "false")))
            .containsExactly((int) ustteki);
        assertThat(idler("/api/v1/demirbaslar", Map.of("markaId", "" + tanim.markaId())))
            .containsExactlyInAnyOrder((int) ustteki, (int) alttaki).doesNotContain((int) baskasi);
        assertThat(idler("/api/v1/demirbaslar", Map.of("modelId", "" + baskaMarka.modelId()))).containsExactly((int) baskasi);
        assertThat(idler("/api/v1/demirbaslar", Map.of("depoId", "" + depo))).containsExactly((int) ustteki);
        assertThat(idler("/api/v1/demirbaslar", Map.of("markaId", "" + tanim.markaId(), "durum", "ARIZALI")))
            .as("filtreler birlikte").containsExactly((int) alttaki);

        assertThat(demirbaslar.delete(alttaki)).hasStatus(204);
        assertThat(idler("/api/v1/demirbaslar", Map.of("kategoriId", "" + ust))).as("silinen listelenmez")
            .containsExactly((int) ustteki);
        assertThat(idler("/api/v1/demirbaslar", Map.of("kategoriId", "999999999"))).isEmpty();
    }

    @Test
    void shouldFilterSarfMalzemeleriByDefinitionsAndSearchText() {
        TanimSeti tanim = veri.sarfTanimlari("S" + ek);
        long toner = sarflar.olustur(EnvanterFixture.sarf("Toner 85A " + ek, tanim));
        long kagit = sarflar.olustur(EnvanterFixture.sarf("A4 Kağıt " + ek, tanim).ile("aciklama", "Beyaz " + ek));

        assertThat(idler("/api/v1/sarf-malzemeleri", Map.of("markaId", "" + tanim.markaId())))
            .containsExactlyInAnyOrder((int) toner, (int) kagit);
        assertThat(idler("/api/v1/sarf-malzemeleri", Map.of("q", "toner 85a " + ek.toLowerCase()))).containsExactly((int) toner);
        assertThat(idler("/api/v1/sarf-malzemeleri", Map.of("q", "beyaz " + ek))).as("açıklamada").containsExactly((int) kagit);
        assertThat(idler("/api/v1/demirbaslar", Map.of("q", "Toner 85A " + ek))).as("türler ayrı").isEmpty();
    }

    // ------------------------------------------------------------------ B: arama ve bulma

    @Test
    void shouldSearchDemirbaslarByTextNumberAndBarcodeAndFindByExactValues() {
        TanimSeti tanim = veri.demirbasTanimlari("Q" + ek);
        long laptop = demirbaslar.olustur(EnvanterFixture.demirbas(tanim).ile("ad", "Laptop " + ek)
            .ile("seriNo", "SN-" + ek).ile("demirbasNo", 7_700_000 + SAYAC.get()));
        long yazici = demirbaslar.olustur(EnvanterFixture.demirbas(tanim).ile("ad", "Yazıcı " + ek));
        String barkod = "869" + ek;
        new ApiIstemcisi(mvc, "/api/v1/envanterler/" + yazici + "/barkodlar").olustur(EnvanterFixture.barkod(barkod));

        assertThat(idler("/api/v1/demirbaslar", Map.of("q", "laptop " + ek.toLowerCase()))).containsExactly((int) laptop);
        assertThat(idler("/api/v1/demirbaslar", Map.of("q", "SN-" + ek))).containsExactly((int) laptop);
        assertThat(idler("/api/v1/demirbaslar", Map.of("q", "" + (7_700_000 + SAYAC.get())))).contains((int) laptop);
        assertThat(idler("/api/v1/demirbaslar", Map.of("q", barkod))).as("barkodda").containsExactly((int) yazici);
        assertThat(idler("/api/v1/demirbaslar", Map.of("q", "%"))).as("joker değil").doesNotContain((int) laptop);

        assertThat(idler("/api/v1/demirbaslar/bul", Map.of("barkod", barkod))).containsExactly((int) yazici);
        assertThat(idler("/api/v1/demirbaslar/bul", Map.of("seriNo", "SN-" + ek))).containsExactly((int) laptop);
        assertThat(idler("/api/v1/demirbaslar/bul", Map.of("barkod", "YOK-" + ek))).isEmpty();
        assertThat(mvc.get().uri("/api/v1/demirbaslar/bul")).hasStatus(400);
        assertThat(mvc.get().uri("/api/v1/demirbaslar/bul").param("barkod", barkod).param("seriNo", "x")).hasStatus(400);
    }

    // ------------------------------------------------------------------ C: tanım listeleri

    @Test
    void shouldFilterDefinitionListsForForms() {
        long dell = veri.marka(MalzemeTuru.DEMIRBAS, "Dell " + ek);
        long hp = veri.marka(MalzemeTuru.DEMIRBAS, "HP " + ek);
        long latitude = veri.demirbasModeli(dell, "Latitude " + ek);
        long vostro = veri.demirbasModeli(dell, "Vostro " + ek);
        long elitebook = veri.demirbasModeli(hp, "EliteBook " + ek);
        long sarfMarka = veri.marka(MalzemeTuru.SARF, "Canon " + ek);

        assertThat(idler("/api/v1/modeller", Map.of("markaId", "" + dell)))
            .containsExactlyInAnyOrder((int) latitude, (int) vostro).doesNotContain((int) elitebook);
        assertThat(idler("/api/v1/modeller", Map.of("ad", "LATITUDE " + ek))).containsExactly((int) latitude);
        assertThat(idler("/api/v1/markalar", Map.of("tur", "SARF", "ad", ek))).containsExactly((int) sarfMarka);
        assertThat(idler("/api/v1/markalar", Map.of("tur", "DEMIRBAS", "ad", ek)))
            .containsExactlyInAnyOrder((int) dell, (int) hp);

        long kok = kategoriler.olustur(TanimFixture.sarfKategorisi("Kırtasiye " + ek));
        long kalem = kategoriler.olustur(TanimFixture.sarfKategorisi("Kalem " + ek).ile("ustKategoriId", kok));
        long kursun = kategoriler.olustur(TanimFixture.sarfKategorisi("Kurşun " + ek).ile("ustKategoriId", kalem));
        assertThat(idler("/api/v1/kategoriler", Map.of("ustKategoriId", "" + kok))).containsExactly((int) kalem);

        MvcTestResult agac = mvc.get().uri("/api/v1/kategoriler/agac").param("tur", "SARF").exchange();
        assertThat(agac).hasStatusOk();
        String govde = ApiIstemcisi.govde(agac);
        List<Integer> altlar = JsonPath.read(govde, "$[?(@.id == " + kok + ")].altKategoriler[*].id");
        List<Integer> torunlar = JsonPath.read(govde, "$[?(@.id == " + kok + ")].altKategoriler[*].altKategoriler[*].id");
        assertThat(altlar).containsExactly((int) kalem);
        assertThat(torunlar).containsExactly((int) kursun);
        assertThat(JsonPath.<List<String>>read(govde, "$[*].tur")).containsOnly("SARF");

        assertThat(mvc.get().uri("/api/v1/firmalar").param("tur", "KAYIP")).hasStatus(400);
    }

    // ------------------------------------------------------------------ D: ürün geçmişi

    @Test
    void shouldListHistoryOfAProductAndMovementsOfADepot() {
        TanimSeti tanim = veri.demirbasTanimlari("D" + ek);
        long demirbas = demirbaslar.olustur(EnvanterFixture.demirbas(tanim));
        long depo = veri.depo("Hareket Deposu " + ek);
        ApiIstemcisi hareketler = new ApiIstemcisi(mvc, "/api/v1/envanter-hareketleri");
        long eski = hareketler.olustur(GecmisKayitFixture.demirbasHareketi(demirbas).ile("kayitTarihi", "2026-01-01T00:00:00"));
        long yeni = hareketler.olustur(GecmisKayitFixture.demirbasHareketi(demirbas).ile("kayitTarihi", "2026-03-01T00:00:00")
            .ile("nereyeDepoId", depo));
        long ariza = new ApiIstemcisi(mvc, "/api/v1/arizalar").olustur(ArizaSayimFixture.ariza().ile("envanterId", demirbas));

        assertThat(idler("/api/v1/demirbaslar/" + demirbas + "/hareketler", Map.of()))
            .as("en yeni önce").containsExactly((int) yeni, (int) eski);
        assertThat(idler("/api/v1/demirbaslar/" + demirbas + "/arizalar", Map.of())).containsExactly((int) ariza);
        assertThat(idler("/api/v1/envanter-hareketleri", Map.of("depoId", "" + depo))).containsExactly((int) yeni);

        long sarf = sarflar.olustur(EnvanterFixture.sarf("Toner " + ek, veri.sarfTanimlari("DS" + ek)));
        long stok = hareketler.olustur(GecmisKayitFixture.sarfHareketi(sarf));
        assertThat(idler("/api/v1/sarf-malzemeleri/" + sarf + "/hareketler", Map.of())).containsExactly((int) stok);

        assertThat(mvc.get().uri("/api/v1/demirbaslar/{id}/hareketler", sarf)).as("türü farklı").hasStatus(404);
        assertThat(mvc.get().uri("/api/v1/sarf-malzemeleri/{id}/hareketler", demirbas)).hasStatus(404);
        assertThat(mvc.get().uri("/api/v1/demirbaslar/{id}/arizalar?sort=sebep", demirbas)).as("izinsiz sıralama")
            .hasStatus(400);
        assertThat(demirbaslar.delete(demirbas)).hasStatus(204);
        assertThat(mvc.get().uri("/api/v1/demirbaslar/{id}/hareketler", demirbas)).as("silinmiş").hasStatus(404);
    }
}
