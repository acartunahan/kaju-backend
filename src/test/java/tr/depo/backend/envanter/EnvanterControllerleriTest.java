package tr.depo.backend.envanter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
import tr.depo.backend.common.exception.OracleHataCevirici;
import tr.depo.backend.envanter.aparat.EnvanterAparatiController;
import tr.depo.backend.envanter.aparat.EnvanterAparatiService;
import tr.depo.backend.envanter.aparat.dto.EnvanterAparatiResponse;
import tr.depo.backend.envanter.barkod.BarkodController;
import tr.depo.backend.envanter.barkod.BarkodService;
import tr.depo.backend.envanter.demirbas.DemirbasController;
import tr.depo.backend.envanter.demirbas.DemirbasService;
import tr.depo.backend.envanter.not.EnvanterNotuController;
import tr.depo.backend.envanter.not.EnvanterNotuService;
import tr.depo.backend.envanter.sarf.SarfMalzemesiController;
import tr.depo.backend.envanter.sarf.SarfMalzemesiService;
import tr.depo.backend.support.fixture.EnvanterFixture;
import tr.depo.backend.support.fixture.IstekGovdesi;

/** Envanter controller'larının web sözleşmesi: yollar (iç içe alt kaynaklar dahil), 201 + Location, doğrulama, 204. */
@WebMvcTest({DemirbasController.class, SarfMalzemesiController.class, BarkodController.class,
    EnvanterNotuController.class, EnvanterAparatiController.class})
@Import(OracleHataCevirici.class)
class EnvanterControllerleriTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private DemirbasService demirbasService;
    @MockitoBean
    private SarfMalzemesiService sarfMalzemesiService;
    @MockitoBean
    private BarkodService barkodService;
    @MockitoBean
    private EnvanterNotuService notService;
    @MockitoBean
    private EnvanterAparatiService aparatService;

    private MvcTestResult post(String yol, IstekGovdesi govde) {
        return mvc.post().uri(yol).contentType(MediaType.APPLICATION_JSON).content(govde.json()).exchange();
    }

    @Test
    void shouldRequireStatusAndRegistrationDateForDemirbas() {
        assertThat(post("/api/v1/demirbaslar", EnvanterFixture.demirbas().olmadan("durum").olmadan("kayitTarihi")))
            .hasStatus(400)
            .bodyJson().hasPathSatisfying("$.hatalar[*].alan",
                alanlar -> assertThat(alanlar).asArray().containsExactlyInAnyOrder("durum", "kayitTarihi"));
        verifyNoInteractions(demirbasService);
    }

    @Test
    void shouldReturn400ForUnknownEnumValue() {
        assertThat(post("/api/v1/demirbaslar", EnvanterFixture.demirbas().ile("durum", "KAYIP")))
            .hasStatus(400)
            .bodyJson().extractingPath("$.kod").isEqualTo("GECERSIZ_ISTEK");
    }

    @Test
    void shouldRequireNameForSarf() {
        assertThat(post("/api/v1/sarf-malzemeleri", EnvanterFixture.sarf(" "))).hasStatus(400);
    }

    @Test
    void shouldCreateChildWithNestedLocation() {
        when(aparatService.olustur(eq(7L), any())).thenReturn(new EnvanterAparatiResponse(3L, 7L, "Şarj adaptörü", 1,
            null, null, 0, null, "API", null, null));

        assertThat(post("/api/v1/envanterler/7/aparatlar", EnvanterFixture.aparat("Şarj adaptörü", 1)))
            .hasStatus(201)
            .hasHeader(HttpHeaders.LOCATION, "http://localhost/api/v1/envanterler/7/aparatlar/3")
            .bodyJson().extractingPath("$.envanterId").isEqualTo(7);
    }

    @Test
    void shouldRejectZeroAparatCount() {
        assertThat(post("/api/v1/envanterler/7/aparatlar", EnvanterFixture.aparat("Kablo", 0)))
            .hasStatus(400)
            .bodyJson().hasPathSatisfying("$.hatalar[0].mesaj",
                mesaj -> assertThat(mesaj).asString().isEqualTo("Sıfırdan büyük olmalıdır."));
    }

    @Test
    void shouldPassBothIdsToChildServices() {
        assertThat(mvc.delete().uri("/api/v1/envanterler/7/barkodlar/3")).hasStatus(204);
        assertThat(mvc.delete().uri("/api/v1/envanterler/7/notlar/4")).hasStatus(204);
        assertThat(mvc.delete().uri("/api/v1/envanterler/7/aparatlar/5")).hasStatus(204);
        verify(barkodService).sil(7L, 3L);
        verify(notService).sil(7L, 4L);
        verify(aparatService).sil(7L, 5L);
    }
}
