package tr.depo.backend.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/** Hata cevaplarının biçimi: RFC 9457 ProblemDetail, Türkçe mesaj, kod, doğru HTTP durumu, stack trace yok. */
@WebMvcTest(controllers = HataUretenTestController.class)
@Import(OracleHataCevirici.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvcTester mvc;

    @Test
    void shouldReturn404ProblemDetailWhenRecordNotFound() {
        assertThat(get("/test/hata/bulunamadi"))
            .hasStatus(404)
            .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
            .bodyJson()
            .hasPathSatisfying("$.kod", kod -> assertThat(kod).asString().isEqualTo("KAYIT_BULUNAMADI"))
            .hasPathSatisfying("$.title", t -> assertThat(t).asString().isEqualTo("Kayıt bulunamadı"))
            .hasPathSatisfying("$.detail", d -> assertThat(d).asString().isEqualTo("Marka bulunamadı (id: 42)."))
            .hasPathSatisfying("$.type", t -> assertThat(t).asString().isEqualTo("urn:kaju:hata:KAYIT_BULUNAMADI"))
            .hasPathSatisfying("$.instance", i -> assertThat(i).asString().isEqualTo("/test/hata/bulunamadi"));
    }

    @Test
    void shouldReturn422WithFieldWhenReferenceIsInvalid() {
        assertThat(get("/test/hata/referans"))
            .hasStatus(422)
            .bodyJson()
            .hasPathSatisfying("$.kod", kod -> assertThat(kod).asString().isEqualTo("REFERANS_GECERSIZ"))
            .hasPathSatisfying("$.alan", alan -> assertThat(alan).asString().isEqualTo("depoId"))
            .hasPathSatisfying("$.detail", d -> assertThat(d).asString().contains("'depoId'").contains("7"));
    }

    @Test
    void shouldReturn409WhenRecordIsInUse() {
        assertThat(get("/test/hata/kullanimda"))
            .hasStatus(409)
            .bodyJson()
            .hasPathSatisfying("$.kod", kod -> assertThat(kod).asString().isEqualTo("KAYIT_KULLANIMDA"))
            .hasPathSatisfying("$.detail", d -> assertThat(d).asString().isEqualTo(
                "Marka (id: 42) silinemez: 3 aktif envanter kaydı buna bağlı."));
    }

    @Test
    void shouldReturn409ForVersionConflictFromServiceAndFromHibernate() {
        assertThat(get("/test/hata/surum")).hasStatus(409)
            .bodyJson().hasPathSatisfying("$.kod", kod -> assertThat(kod).asString().isEqualTo("SURUM_CAKISMASI"));
        assertThat(get("/test/hata/iyimser-kilit")).hasStatus(409)
            .bodyJson().hasPathSatisfying("$.detail", d -> assertThat(d).asString().startsWith("Marka (id: 42)"));
    }

    @Test
    void shouldTranslateOracleUniqueViolationToConflictWithConstraintMessage() {
        assertThat(get("/test/hata/tekil")).hasStatus(409)
            .bodyJson()
            .hasPathSatisfying("$.kod", kod -> assertThat(kod).asString().isEqualTo("TEKILLIK_IHLALI"))
            .hasPathSatisfying("$.detail", d -> assertThat(d).asString()
                .isEqualTo("Bu dosya anahtarı başka bir fotoğrafta kullanılıyor."));
    }

    @Test
    void shouldFallBackToGenericMessageForConstraintWithoutOwnMessage() {
        assertThat(get("/test/hata/tekil-bilinmeyen")).hasStatus(409)
            .bodyJson().hasPathSatisfying("$.detail", d -> assertThat(d).asString()
                .isEqualTo("Bu değer başka bir kayıtta kullanılıyor."));
    }

    @Test
    void shouldReturn500WithoutInternalDetailsForPhysicalDeleteTriggerAndUnexpectedErrors() {
        assertThat(get("/test/hata/fiziksel-silme")).hasStatus(500)
            .bodyJson().hasPathSatisfying("$.kod", kod -> assertThat(kod).asString().isEqualTo("BEKLENMEYEN_HATA"));

        MvcTestResult sonuc = get("/test/hata/beklenmeyen");
        assertThat(sonuc).hasStatus(500);
        assertThat(sonuc).bodyText().doesNotContain("şifre").doesNotContain("IllegalStateException")
            .doesNotContain("trace").doesNotContain("at tr.depo");
    }

    @Test
    void shouldReturnFieldErrorsWhenValidationFails() {
        MvcTestResult sonuc = mvc.post().uri("/test/hata/dogrulama")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"ad\": \"cok-uzun-ad\"}")
            .exchange();

        assertThat(sonuc).hasStatus(400)
            .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
            .bodyJson()
            .hasPathSatisfying("$.kod", kod -> assertThat(kod).asString().isEqualTo("DOGRULAMA_HATASI"))
            .hasPathSatisfying("$.detail", d -> assertThat(d).asString().isEqualTo("İstekte 2 alan hatası var."))
            .hasPathSatisfying("$.hatalar[?(@.alan == 'ad')].mesaj",
                m -> assertThat(m).asArray().containsExactly("En fazla 5 karakter olabilir."))
            .hasPathSatisfying("$.hatalar[?(@.alan == 'depoId')].mesaj",
                m -> assertThat(m).asArray().containsExactly("Bu alan zorunludur."));
    }

    @Test
    void shouldReturn400ForMalformedJson() {
        assertThat(mvc.post().uri("/test/hata/dogrulama").contentType(MediaType.APPLICATION_JSON).content("{bozuk").exchange())
            .hasStatus(400)
            .bodyJson().hasPathSatisfying("$.kod", kod -> assertThat(kod).asString().isEqualTo("GECERSIZ_ISTEK"));
    }

    @Test
    void shouldReturn400WhenSortingByNotAllowedField() {
        assertThat(get("/test/hata/siralama?sort=gizliAlan,desc"))
            .hasStatus(400)
            .bodyJson()
            .hasPathSatisfying("$.kod", kod -> assertThat(kod).asString().isEqualTo("GECERSIZ_SIRALAMA"))
            .hasPathSatisfying("$.detail", d -> assertThat(d).asString().contains("'gizliAlan'"));
    }

    @Test
    void shouldReturn405ProblemDetailForUnsupportedMethod() {
        assertThat(mvc.delete().uri("/test/hata/bulunamadi").exchange())
            .hasStatus(405)
            .bodyJson().hasPathSatisfying("$.kod", kod -> assertThat(kod).asString().isEqualTo("METOT_DESTEKLENMIYOR"));
    }

    private MvcTestResult get(String yol) {
        return mvc.get().uri(yol).accept(MediaType.APPLICATION_JSON).exchange();
    }
}
