package tr.depo.backend.ariza.islem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import tr.depo.domain.ariza.ArizaIslemTuru;

/**
 * Arızaya işlem kaydı (işlem, bilgi, şikayet, teşekkür). Sınırlar ARIZA_ISLEMI tablosuyla aynıdır.
 *
 * @param islemKodu legacy ham işlem kodu (anlamı doğrulanmadı)
 */
public record CreateArizaIslemiRequest(
    @NotNull(message = "{dogrulama.zorunlu}")
    ArizaIslemTuru tur,

    @NotNull(message = "{dogrulama.zorunlu}")
    BigDecimal islemKodu,

    @NotBlank(message = "{dogrulama.bos-olamaz}")
    @Size(max = 36, message = "{dogrulama.en-fazla}")
    String islemYapanSicil,

    @NotBlank(message = "{dogrulama.bos-olamaz}")
    @Size(max = 36, message = "{dogrulama.en-fazla}")
    String gorevliSicil,

    @NotNull(message = "{dogrulama.zorunlu}")
    LocalDateTime zaman,

    @NotNull(message = "{dogrulama.zorunlu}")
    LocalDateTime miad,

    String aciklama,

    Long siraNo
) {
}
