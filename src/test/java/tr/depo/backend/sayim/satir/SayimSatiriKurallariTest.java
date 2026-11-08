package tr.depo.backend.sayim.satir;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import tr.depo.backend.common.exception.IsKuraliException;
import tr.depo.domain.sayim.SayimIslemi;
import tr.depo.domain.sayim.SayimKapsami;
import tr.depo.domain.sayim.YerelSayimTuru;

/** Sayım satırının türe göre domain kuralları (422'ye çevrilir). */
class SayimSatiriKurallariTest {

    private static final LocalDateTime TARIH = LocalDateTime.of(2026, 1, 1, 0, 0);

    @Test
    void shouldRejectOperationThatIsNotValidForTheRowType() {
        assertThatThrownBy(() -> SayimSatiriKurallari.dogrula(SayimKapsami.YEREL, SayimIslemi.DUZELTME, null,
                YerelSayimTuru.ZIMMET, "M", "Z", null))
            .isInstanceOf(IsKuraliException.class)
            .extracting(hata -> ((IsKuraliException) hata).mesajAnahtari())
            .isEqualTo(SayimSatiriKurallari.ISLEM_TURE_UYGUN_DEGIL);
        assertThatThrownBy(() -> SayimSatiriKurallari.dogrula(SayimKapsami.GENEL, SayimIslemi.REZERVE, TARIH,
                null, null, null, null))
            .isInstanceOf(IsKuraliException.class);
    }

    @Test
    void shouldRequireMaterialZimmetAndMaterialTypeForLocalRow() {
        assertThatThrownBy(() -> SayimSatiriKurallari.dogrula(SayimKapsami.YEREL, SayimIslemi.SAYILDI, null,
                YerelSayimTuru.SARF, "M", " ", null))
            .isInstanceOf(IsKuraliException.class);
        assertThatThrownBy(() -> SayimSatiriKurallari.dogrula(SayimKapsami.YEREL, SayimIslemi.SAYILDI, null,
                null, "M", "Z", null))
            .isInstanceOf(IsKuraliException.class);
    }

    @Test
    void shouldRequireRowTypeToMatchSayimScope() {
        assertThatThrownBy(() -> SayimSatiriKurallari.kapsamaUygunOlmali(SayimKapsami.GENEL, SayimKapsami.YEREL))
            .isInstanceOf(IsKuraliException.class);
        assertThatThrownBy(() -> SayimSatiriKurallari.kapsamaUygunOlmali(SayimKapsami.YEREL, SayimKapsami.GENEL))
            .isInstanceOf(IsKuraliException.class);
        assertThatCode(() -> SayimSatiriKurallari.kapsamaUygunOlmali(SayimKapsami.YEREL, SayimKapsami.YEREL))
            .doesNotThrowAnyException();
    }

    @Test
    void shouldAllowEnvanterOnlyForZimmetLocalRow() {
        assertThatThrownBy(() -> SayimSatiriKurallari.dogrula(SayimKapsami.YEREL, SayimIslemi.SAYILDI, null,
                YerelSayimTuru.SARF, "M", "Z", 5L))
            .isInstanceOf(IsKuraliException.class);
        assertThatCode(() -> SayimSatiriKurallari.dogrula(SayimKapsami.YEREL, SayimIslemi.ZIMMET_DUZELTME, null,
                YerelSayimTuru.ZIMMET, "M", "Z", 5L))
            .doesNotThrowAnyException();
    }

    @Test
    void shouldRequireOperationDateForGeneralRow() {
        assertThatThrownBy(() -> SayimSatiriKurallari.dogrula(SayimKapsami.GENEL, SayimIslemi.SAYILDI, null,
                null, null, null, 5L))
            .isInstanceOf(IsKuraliException.class);
        assertThatCode(() -> SayimSatiriKurallari.dogrula(SayimKapsami.GENEL, SayimIslemi.DUZELTME, TARIH,
                null, null, null, 5L))
            .doesNotThrowAnyException();
    }
}
