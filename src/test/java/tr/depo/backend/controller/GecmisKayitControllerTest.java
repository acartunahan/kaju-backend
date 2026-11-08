package tr.depo.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import tr.depo.backend.exception.OracleHataCevirici;
import tr.depo.backend.service.EnvanterHareketiService;
import tr.depo.backend.service.ZimmetKaydiService;
import tr.depo.backend.util.GecmisKayitFixture;

/** Geçmiş kayıtlarının web sözleşmesi: yalnızca GET/POST; PUT ve DELETE 405. */
@WebMvcTest({EnvanterHareketiController.class, ZimmetKaydiController.class})
@Import(OracleHataCevirici.class)
class GecmisKayitControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private EnvanterHareketiService hareketService;
    @MockitoBean
    private ZimmetKaydiService zimmetKaydiService;

    @Test
    void shouldNotOfferUpdateOrDeleteForHistoryRecords() {
        String govde = GecmisKayitFixture.zimmetKaydi().json();
        assertThat(mvc.delete().uri("/api/v1/envanter-hareketleri/1")).hasStatus(405)
            .bodyJson().extractingPath("$.kod").isEqualTo("METOT_DESTEKLENMIYOR");
        assertThat(mvc.put().uri("/api/v1/envanter-hareketleri/1").contentType(MediaType.APPLICATION_JSON).content(govde))
            .hasStatus(405);
        assertThat(mvc.delete().uri("/api/v1/zimmet-kayitlari/1")).hasStatus(405);
        assertThat(mvc.put().uri("/api/v1/zimmet-kayitlari/1").contentType(MediaType.APPLICATION_JSON).content(govde))
            .hasStatus(405);
        verifyNoInteractions(hareketService, zimmetKaydiService);
    }

    @Test
    void shouldRequireTypeForMovementAndSicilAndOperationForZimmet() {
        assertThat(mvc.post().uri("/api/v1/envanter-hareketleri").contentType(MediaType.APPLICATION_JSON)
                .content(GecmisKayitFixture.sarfHareketi(1).olmadan("tur").json()))
            .hasStatus(400).bodyJson().extractingPath("$.hatalar[0].alan").isEqualTo("tur");
        assertThat(mvc.post().uri("/api/v1/zimmet-kayitlari").contentType(MediaType.APPLICATION_JSON)
                .content(GecmisKayitFixture.zimmetKaydi().ile("sicil", "").olmadan("islem").json()))
            .hasStatus(400)
            .bodyJson().hasPathSatisfying("$.hatalar[*].alan",
                alanlar -> assertThat(alanlar).asArray().containsExactlyInAnyOrder("sicil", "islem"));
    }
}
