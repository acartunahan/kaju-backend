package tr.depo.backend.serviceimpl;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import tr.depo.backend.exception.IsKuraliException;
import tr.depo.domain.sayim.SayimIslemi;
import tr.depo.domain.sayim.SayimKapsami;
import tr.depo.domain.sayim.YerelSayimTuru;

/** Sayım satırının türe göre domain kuralları (422'ye çevrilir). */
class SayimSatiriServiceImplTest {

    private static final LocalDateTime TARIH = LocalDateTime.of(2026, 1, 1, 0, 0);

    @Test
    void shouldRejectOperationThatIsNotValidForTheRowType() {
        assertThatThrownBy(() -> SayimSatiriServiceImpl.dogrula(SayimKapsami.YEREL, SayimIslemi.DUZELTME, null,
                YerelSayimTuru.ZIMMET, "M", "Z", null))
            .isInstanceOf(IsKuraliException.class)
            .extracting(hata -> ((IsKuraliException) hata).mesajAnahtari())
            .isEqualTo(SayimSatiriServiceImpl.ISLEM_TURE_UYGUN_DEGIL);
        assertThatThrownBy(() -> SayimSatiriServiceImpl.dogrula(SayimKapsami.GENEL, SayimIslemi.REZERVE, TARIH,
                null, null, null, null))
            .isInstanceOf(IsKuraliException.class);
    }

    @Test
    void shouldRequireMaterialZimmetAndMaterialTypeForLocalRow() {
        assertThatThrownBy(() -> SayimSatiriServiceImpl.dogrula(SayimKapsami.YEREL, SayimIslemi.SAYILDI, null,
                YerelSayimTuru.SARF, "M", " ", null))
            .isInstanceOf(IsKuraliException.class);
        assertThatThrownBy(() -> SayimSatiriServiceImpl.dogrula(SayimKapsami.YEREL, SayimIslemi.SAYILDI, null,
                null, "M", "Z", null))
            .isInstanceOf(IsKuraliException.class);
    }

    @Test
    void shouldRequireRowTypeToMatchSayimScope() {
        assertThatThrownBy(() -> SayimSatiriServiceImpl.kapsamaUygunOlmali(SayimKapsami.GENEL, SayimKapsami.YEREL))
            .isInstanceOf(IsKuraliException.class);
        assertThatThrownBy(() -> SayimSatiriServiceImpl.kapsamaUygunOlmali(SayimKapsami.YEREL, SayimKapsami.GENEL))
            .isInstanceOf(IsKuraliException.class);
        assertThatCode(() -> SayimSatiriServiceImpl.kapsamaUygunOlmali(SayimKapsami.YEREL, SayimKapsami.YEREL))
            .doesNotThrowAnyException();
    }

    @Test
    void shouldAllowEnvanterOnlyForZimmetLocalRow() {
        assertThatThrownBy(() -> SayimSatiriServiceImpl.dogrula(SayimKapsami.YEREL, SayimIslemi.SAYILDI, null,
                YerelSayimTuru.SARF, "M", "Z", 5L))
            .isInstanceOf(IsKuraliException.class);
        assertThatCode(() -> SayimSatiriServiceImpl.dogrula(SayimKapsami.YEREL, SayimIslemi.ZIMMET_DUZELTME, null,
                YerelSayimTuru.ZIMMET, "M", "Z", 5L))
            .doesNotThrowAnyException();
    }

    @Test
    void shouldRequireOperationDateForGeneralRow() {
        assertThatThrownBy(() -> SayimSatiriServiceImpl.dogrula(SayimKapsami.GENEL, SayimIslemi.SAYILDI, null,
                null, null, null, 5L))
            .isInstanceOf(IsKuraliException.class);
        assertThatCode(() -> SayimSatiriServiceImpl.dogrula(SayimKapsami.GENEL, SayimIslemi.DUZELTME, TARIH,
                null, null, null, 5L))
            .doesNotThrowAnyException();
    }
}
