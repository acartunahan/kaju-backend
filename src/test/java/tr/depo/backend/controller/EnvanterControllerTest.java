package tr.depo.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import tr.depo.backend.dto.DemirbasFiltreRequest;
import tr.depo.backend.dto.EnvanterAparatiResponse;
import tr.depo.backend.dto.SarfMalzemesiFiltreRequest;
import tr.depo.backend.exception.OracleHataCevirici;
import tr.depo.backend.service.ArizaService;
import tr.depo.backend.service.BarkodService;
import tr.depo.backend.service.DemirbasService;
import tr.depo.backend.service.EnvanterAparatiService;
import tr.depo.backend.service.EnvanterHareketiService;
import tr.depo.backend.service.EnvanterNotuService;
import tr.depo.backend.service.SarfMalzemesiService;
import tr.depo.backend.service.ZimmetGecmisiService;
import tr.depo.backend.util.EnvanterFixture;
import tr.depo.backend.util.IstekGovdesi;
import tr.depo.backend.util.TanimSeti;
import tr.depo.domain.envanter.DemirbasDurumu;

/** Envanter controller'larının web sözleşmesi: yollar (iç içe alt kaynaklar dahil), 201 + Location, doğrulama, 204. */
@WebMvcTest({DemirbasController.class, SarfMalzemesiController.class, BarkodController.class,
    EnvanterNotuController.class, EnvanterAparatiController.class, ZimmetGecmisiController.class})
@Import(OracleHataCevirici.class)
class EnvanterControllerTest {

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
    @MockitoBean
    private ZimmetGecmisiService zimmetGecmisiService;
    @MockitoBean
    private EnvanterHareketiService hareketService;
    @MockitoBean
    private ArizaService arizaService;

    private MvcTestResult post(String yol, IstekGovdesi govde) {
        return mvc.post().uri(yol).contentType(MediaType.APPLICATION_JSON).content(govde.json()).exchange();
    }

    @Test
    void shouldRequireStatusAndRegistrationDateForDemirbas() {
        assertThat(post("/api/v1/demirbaslar", EnvanterFixture.demirbas(TanimSeti.SAHTE).olmadan("durum").olmadan("kayitTarihi")))
            .hasStatus(400)
            .bodyJson().hasPathSatisfying("$.hatalar[*].alan",
                alanlar -> assertThat(alanlar).asArray().containsExactlyInAnyOrder("durum", "kayitTarihi"));
        verifyNoInteractions(demirbasService);
    }

    @Test
    void shouldRequireCategoryBrandAndModelForNewDemirbasAndSarf() {
        for (String yol : new String[] {"/api/v1/demirbaslar", "/api/v1/sarf-malzemeleri"}) {
            IstekGovdesi govde = yol.endsWith("demirbaslar")
                ? EnvanterFixture.demirbas(TanimSeti.SAHTE)
                : EnvanterFixture.sarf("Toner", TanimSeti.SAHTE);
            assertThat(post(yol, govde.olmadan("kategoriId").olmadan("markaId").olmadan("modelId")))
                .as(yol).hasStatus(400)
                .bodyJson().hasPathSatisfying("$.hatalar[*].alan",
                    alanlar -> assertThat(alanlar).asArray().containsExactlyInAnyOrder("kategoriId", "markaId", "modelId"));
        }
        verifyNoInteractions(demirbasService, sarfMalzemesiService);
    }

    @Test
    void shouldReturn400ForUnknownEnumValue() {
        assertThat(post("/api/v1/demirbaslar", EnvanterFixture.demirbas(TanimSeti.SAHTE).ile("durum", "KAYIP")))
            .hasStatus(400)
            .bodyJson().extractingPath("$.kod").isEqualTo("GECERSIZ_ISTEK");
    }

    @Test
    void shouldRequireNameForSarf() {
        assertThat(post("/api/v1/sarf-malzemeleri", EnvanterFixture.sarf(" ", TanimSeti.SAHTE))).hasStatus(400);
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
    void shouldBindAllDemirbasFiltersFromQueryParameters() {
        assertThat(mvc.get().uri("/api/v1/demirbaslar").param("kategoriId", "3").param("altKategoriler", "false")
            .param("markaId", "4").param("modelId", "5").param("depoId", "6").param("ihaleId", "7")
            .param("durum", "NORMAL").param("zimmetliSicil", "0012345").param("q", "laptop")).hasStatusOk();

        ArgumentCaptor<DemirbasFiltreRequest> filtre = ArgumentCaptor.forClass(DemirbasFiltreRequest.class);
        verify(demirbasService).listele(filtre.capture(), any(Pageable.class));
        assertThat(filtre.getValue()).isEqualTo(new DemirbasFiltreRequest(3L, false, 4L, 5L, 6L, 7L,
            DemirbasDurumu.NORMAL, "0012345", "laptop"));

        assertThat(mvc.get().uri("/api/v1/demirbaslar")).hasStatusOk();
        verify(demirbasService).listele(eq(new DemirbasFiltreRequest(null, null, null, null, null, null, null, null, null)),
            any(Pageable.class));
        assertThat(mvc.get().uri("/api/v1/demirbaslar").param("durum", "KAYIP")).as("bilinmeyen durum").hasStatus(400);
        verifyNoMoreInteractions(demirbasService);
    }

    @Test
    void shouldRouteLookupAndHistoryEndpointsOfDemirbasAndSarf() {
        assertThat(mvc.get().uri("/api/v1/demirbaslar/bul").param("barkod", "8690001")).hasStatusOk();
        verify(demirbasService).bul("8690001", null, null);

        assertThat(mvc.get().uri("/api/v1/demirbaslar/7/hareketler")).hasStatusOk();
        verify(hareketService).demirbasHareketleri(eq(7L), any(Pageable.class));
        assertThat(mvc.get().uri("/api/v1/sarf-malzemeleri/8/hareketler")).hasStatusOk();
        verify(hareketService).sarfHareketleri(eq(8L), any(Pageable.class));
        assertThat(mvc.get().uri("/api/v1/demirbaslar/7/arizalar")).hasStatusOk();
        verify(arizaService).demirbasArizalari(eq(7L), any(Pageable.class));

        assertThat(mvc.get().uri("/api/v1/sarf-malzemeleri").param("kategoriId", "3").param("q", "toner")).hasStatusOk();
        verify(sarfMalzemesiService).listele(eq(new SarfMalzemesiFiltreRequest(3L, null, null, null, null, "toner")),
            any(Pageable.class));
    }

    @Test
    void shouldServeZimmetHistoryOnlyForReading() {
        assertThat(mvc.get().uri("/api/v1/demirbaslar/7/zimmet-gecmisi")).hasStatusOk();
        verify(zimmetGecmisiService).listele(eq(7L), any(Pageable.class));
        assertThat(mvc.post().uri("/api/v1/demirbaslar/7/zimmet-gecmisi").contentType(MediaType.APPLICATION_JSON)
            .content("{}")).hasStatus(405);
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
