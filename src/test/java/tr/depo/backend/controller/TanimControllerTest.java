package tr.depo.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import tr.depo.backend.dto.DepoResponse;
import tr.depo.backend.exception.OracleHataCevirici;
import tr.depo.backend.service.DepoService;
import tr.depo.backend.service.FirmaService;
import tr.depo.backend.service.IhaleService;
import tr.depo.backend.service.KategoriService;
import tr.depo.backend.service.ModelService;
import tr.depo.backend.util.IstekGovdesi;
import tr.depo.backend.util.TanimFixture;

/**
 * Tanım controller'larının web sözleşmesi: yollar, 201 + Location, 204, alan bazlı doğrulama. Hata biçimi ve
 * diğer durum kodları ortak olduğu için GlobalExceptionHandlerTest ve MarkaControllerTest'te sınanır.
 */
@WebMvcTest({DepoController.class, FirmaController.class, KategoriController.class, ModelController.class,
    IhaleController.class})
@Import(OracleHataCevirici.class)
class TanimControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private DepoService depoService;
    @MockitoBean
    private FirmaService firmaService;
    @MockitoBean
    private KategoriService kategoriService;
    @MockitoBean
    private ModelService modelService;
    @MockitoBean
    private IhaleService ihaleService;

    private MvcTestResult post(String yol, IstekGovdesi govde) {
        return mvc.post().uri(yol).contentType(MediaType.APPLICATION_JSON).content(govde.json()).exchange();
    }

    @Test
    void shouldCreateDepoWith201AndLocation() {
        when(depoService.olustur(any())).thenReturn(new DepoResponse(5L, "Merkez Depo", null, null, null, null, 0,
            null, "API", null, null));

        assertThat(post("/api/v1/depolar", TanimFixture.depo()))
            .hasStatus(201)
            .hasHeader(HttpHeaders.LOCATION, "http://localhost/api/v1/depolar/5");
    }

    @Test
    void shouldReturnFieldErrorsForInvalidDepo() {
        assertThat(post("/api/v1/depolar", TanimFixture.depo().ile("ad", "").ile("sorumluSicil", "12345678901")))
            .hasStatus(400)
            .bodyJson().hasPathSatisfying("$.hatalar[*].alan",
                alanlar -> assertThat(alanlar).asArray().containsExactlyInAnyOrder("ad", "sorumluSicil"));
    }

    @Test
    void shouldRequireTypeForTypedDefinitions() {
        assertThat(post("/api/v1/firmalar", TanimFixture.sarfFirmasi().olmadan("tur"))).hasStatus(400);
        assertThat(post("/api/v1/kategoriler", TanimFixture.sarfKategorisi("Toner").olmadan("tur"))).hasStatus(400);
        assertThat(post("/api/v1/modeller", TanimFixture.demirbasModeli("X").olmadan("tur"))).hasStatus(400);
    }

    @Test
    void shouldRequireStatusCodeAndRegistrationDateForIhale() {
        assertThat(post("/api/v1/ihaleler", TanimFixture.ihale().olmadan("durumKodu").olmadan("kayitTarihi")))
            .hasStatus(400)
            .bodyJson().hasPathSatisfying("$.hatalar[*].alan",
                alanlar -> assertThat(alanlar).asArray().containsExactlyInAnyOrder("durumKodu", "kayitTarihi"));
    }

    @Test
    void shouldRequireVersionOnUpdate() {
        assertThat(mvc.put().uri("/api/v1/kategoriler/3").contentType(MediaType.APPLICATION_JSON)
                .content(TanimFixture.sarfKategorisi("Toner").olmadan("tur").json()))
            .hasStatus(400)
            .bodyJson().hasPathSatisfying("$.hatalar[0].alan", alan -> assertThat(alan).asString().isEqualTo("versiyon"));
    }

    @Test
    void shouldReturn204OnDeleteForEveryDefinition() {
        assertThat(mvc.delete().uri("/api/v1/depolar/1")).hasStatus(204);
        assertThat(mvc.delete().uri("/api/v1/firmalar/2")).hasStatus(204);
        assertThat(mvc.delete().uri("/api/v1/kategoriler/3")).hasStatus(204);
        assertThat(mvc.delete().uri("/api/v1/modeller/4")).hasStatus(204);
        assertThat(mvc.delete().uri("/api/v1/ihaleler/5")).hasStatus(204);
        verify(depoService).sil(1L);
        verify(firmaService).sil(2L);
        verify(kategoriService).sil(3L);
        verify(modelService).sil(4L);
        verify(ihaleService).sil(5L);
    }
}
