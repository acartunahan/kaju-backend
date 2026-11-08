package tr.depo.backend.ariza;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import tr.depo.backend.ariza.islem.ArizaIslemiController;
import tr.depo.backend.ariza.islem.ArizaIslemiService;
import tr.depo.backend.common.exception.OracleHataCevirici;
import tr.depo.backend.sayim.SayimController;
import tr.depo.backend.sayim.SayimService;
import tr.depo.backend.sayim.satir.SayimSatiriController;
import tr.depo.backend.sayim.satir.SayimSatiriService;
import tr.depo.backend.support.fixture.ArizaSayimFixture;
import tr.depo.backend.support.fixture.IstekGovdesi;

/** Arıza ve sayım controller'larının web sözleşmesi: zorunlu alanlar, iç içe yollar, 204. */
@WebMvcTest({ArizaController.class, ArizaIslemiController.class, SayimController.class, SayimSatiriController.class})
@Import(OracleHataCevirici.class)
class ArizaSayimControllerleriTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private ArizaService arizaService;
    @MockitoBean
    private ArizaIslemiService islemService;
    @MockitoBean
    private SayimService sayimService;
    @MockitoBean
    private SayimSatiriService satirService;

    private MvcTestResult post(String yol, IstekGovdesi govde) {
        return mvc.post().uri(yol).contentType(MediaType.APPLICATION_JSON).content(govde.json()).exchange();
    }

    @Test
    void shouldRequireMandatoryArizaFields() {
        assertThat(post("/api/v1/arizalar", ArizaSayimFixture.ariza().olmadan("sebep").olmadan("tarih").ile("bildirenSicil", " ")))
            .hasStatus(400)
            .bodyJson().hasPathSatisfying("$.hatalar[*].alan",
                alanlar -> assertThat(alanlar).asArray().containsExactlyInAnyOrder("sebep", "tarih", "bildirenSicil"));
        verifyNoInteractions(arizaService);
    }

    @Test
    void shouldRequireMandatoryOperationFields() {
        assertThat(post("/api/v1/arizalar/3/islemler", ArizaSayimFixture.arizaIslemi().olmadan("miad").olmadan("tur")))
            .hasStatus(400)
            .bodyJson().hasPathSatisfying("$.hatalar[*].alan",
                alanlar -> assertThat(alanlar).asArray().containsExactlyInAnyOrder("miad", "tur"));
    }

    @Test
    void shouldRequireRowTypeAndOperation() {
        assertThat(post("/api/v1/sayimlar/4/satirlar", ArizaSayimFixture.genelSatir().olmadan("tur").olmadan("islem")))
            .hasStatus(400)
            .bodyJson().hasPathSatisfying("$.hatalar[*].alan",
                alanlar -> assertThat(alanlar).asArray().containsExactlyInAnyOrder("tur", "islem"));
    }

    @Test
    void shouldReturn400ForOperationValueOutsideEnum() {
        assertThat(post("/api/v1/sayimlar/4/satirlar", ArizaSayimFixture.genelSatir().ile("islem", "BULUNDU")))
            .hasStatus(400).bodyJson().extractingPath("$.kod").isEqualTo("GECERSIZ_ISTEK");
    }

    @Test
    void shouldPassParentAndChildIdsOnDelete() {
        assertThat(mvc.delete().uri("/api/v1/arizalar/3/islemler/8")).hasStatus(204);
        assertThat(mvc.delete().uri("/api/v1/sayimlar/4/satirlar/9")).hasStatus(204);
        assertThat(mvc.delete().uri("/api/v1/sayimlar/4")).hasStatus(204);
        verify(islemService).sil(3L, 8L);
        verify(satirService).sil(4L, 9L);
        verify(sayimService).sil(4L);
    }
}
