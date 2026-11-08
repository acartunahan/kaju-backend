package tr.depo.backend.marka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import tr.depo.backend.common.exception.KayitBulunamadiException;
import tr.depo.backend.common.exception.KayitKullanimdaException;
import tr.depo.backend.common.exception.OracleHataCevirici;
import tr.depo.backend.common.exception.SurumCakismasiException;
import tr.depo.backend.common.paging.SayfaCevabi;
import tr.depo.backend.marka.dto.CreateMarkaRequest;
import tr.depo.backend.marka.dto.MarkaResponse;
import tr.depo.backend.support.fixture.MarkaFixture;
import tr.depo.domain.tanim.MalzemeTuru;

/** Web katmanı: yollar, durum kodları, doğrulama ve hata formatı. Servis mock'tur. */
@WebMvcTest(MarkaController.class)
@Import(OracleHataCevirici.class)
class MarkaControllerTest {

    private static final String YOL = "/api/v1/markalar";
    private static final LocalDateTime ZAMAN = LocalDateTime.of(2026, 1, 1, 9, 0);

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private MarkaService markaService;

    private static MarkaResponse cevap(long id) {
        return new MarkaResponse(id, MalzemeTuru.DEMIRBAS, "Test Marka", null, "1", ZAMAN, null, null, 0, ZAMAN, "API",
            null, null);
    }

    @Test
    void shouldReturn201WithLocationHeaderWhenCreated() {
        when(markaService.olustur(any(CreateMarkaRequest.class))).thenReturn(cevap(10));

        assertThat(mvc.post().uri(YOL).contentType(MediaType.APPLICATION_JSON).content(MarkaFixture.demirbasMarkasi().json()))
            .hasStatus(201)
            .hasHeader(HttpHeaders.LOCATION, "http://localhost/api/v1/markalar/10")
            .bodyJson().extractingPath("$.id").isEqualTo(10);
    }

    @Test
    void shouldReturnFieldErrorsWhenCreateRequestIsInvalid() {
        String gecersiz = MarkaFixture.demirbasMarkasi().tur(null).ad(" ").durumKodu("12").json();

        assertThat(mvc.post().uri(YOL).contentType(MediaType.APPLICATION_JSON).content(gecersiz))
            .hasStatus(400)
            .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
            .bodyJson()
            .hasPathSatisfying("$.kod", kod -> assertThat(kod).asString().isEqualTo("DOGRULAMA_HATASI"))
            .hasPathSatisfying("$.hatalar[*].alan", alanlar -> assertThat(alanlar).asArray()
                .containsExactlyInAnyOrder("tur", "ad", "durumKodu"));
        verifyNoInteractions(markaService);
    }

    @Test
    void shouldReturn400WhenAdExceedsColumnLength() {
        assertThat(mvc.post().uri(YOL).contentType(MediaType.APPLICATION_JSON)
                .content(MarkaFixture.demirbasMarkasi().ad("x".repeat(256)).json()))
            .hasStatus(400)
            .bodyJson().hasPathSatisfying("$.hatalar[0].mesaj",
                m -> assertThat(m).asString().isEqualTo("En fazla 255 karakter olabilir."));
    }

    @Test
    void shouldReturn200WithMarka() {
        when(markaService.getir(10L)).thenReturn(cevap(10));

        assertThat(mvc.get().uri(YOL + "/10"))
            .hasStatusOk()
            .bodyJson().extractingPath("$.ad").isEqualTo("Test Marka");
    }

    @Test
    void shouldReturn404WhenMarkaIsMissingOrSoftDeleted() {
        when(markaService.getir(10L)).thenThrow(new KayitBulunamadiException("Marka", 10L));

        assertThat(mvc.get().uri(YOL + "/10"))
            .hasStatus(404)
            .bodyJson().hasPathSatisfying("$.detail", d -> assertThat(d).asString().isEqualTo("Marka bulunamadı (id: 10)."));
    }

    @Test
    void shouldReturnOwnPageModelAndCapPageSize() {
        when(markaService.listele(any(Pageable.class))).thenReturn(new SayfaCevabi<>(List.of(cevap(1)), 0, 100, 1, 1));

        assertThat(mvc.get().uri(YOL + "?page=0&size=500&sort=ad,asc"))
            .hasStatusOk()
            .bodyJson()
            .hasPathSatisfying("$.icerik[0].id", id -> assertThat(id).asNumber().isEqualTo(1))
            .hasPathSatisfying("$.toplamEleman", t -> assertThat(t).asNumber().isEqualTo(1));

        ArgumentCaptor<Pageable> sayfa = ArgumentCaptor.forClass(Pageable.class);
        verify(markaService).listele(sayfa.capture());
        assertThat(sayfa.getValue().getPageSize()).as("sayfa boyutu üst sınırı").isEqualTo(100);
        assertThat(sayfa.getValue().getSort().getOrderFor("ad")).isNotNull();
    }

    @Test
    void shouldReturn400WhenUpdateHasNoVersion() {
        assertThat(mvc.put().uri(YOL + "/10").contentType(MediaType.APPLICATION_JSON)
                .content(MarkaFixture.demirbasMarkasi().versiyon(null).guncellemeJson()))
            .hasStatus(400)
            .bodyJson().hasPathSatisfying("$.hatalar[0].alan", a -> assertThat(a).asString().isEqualTo("versiyon"));
        verifyNoInteractions(markaService);
    }

    @Test
    void shouldReturn409WhenUpdateVersionIsStale() {
        when(markaService.guncelle(eq(10L), any())).thenThrow(new SurumCakismasiException("Marka", 10L));

        assertThat(mvc.put().uri(YOL + "/10").contentType(MediaType.APPLICATION_JSON)
                .content(MarkaFixture.demirbasMarkasi().guncellemeJson()))
            .hasStatus(409)
            .bodyJson().hasPathSatisfying("$.kod", k -> assertThat(k).asString().isEqualTo("SURUM_CAKISMASI"));
    }

    @Test
    void shouldReturn204WhenDeleted() {
        assertThat(mvc.delete().uri(YOL + "/10")).hasStatus(204);
        verify(markaService).sil(10L);
    }

    @Test
    void shouldReturn409WhenDeletingMarkaInUse() {
        doThrow(new KayitKullanimdaException("Marka", 10L, "model", 2)).when(markaService).sil(10L);

        assertThat(mvc.delete().uri(YOL + "/10"))
            .hasStatus(409)
            .bodyJson().hasPathSatisfying("$.kod", k -> assertThat(k).asString().isEqualTo("KAYIT_KULLANIMDA"));
    }
}
