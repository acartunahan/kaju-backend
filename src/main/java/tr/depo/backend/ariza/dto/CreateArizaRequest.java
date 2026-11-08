package tr.depo.backend.ariza.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Yeni arıza kaydı. Sınırlar ARIZA tablosuyla aynıdır.
 *
 * @param asamaKodu legacy ham aşama kodu (anlamı doğrulanmadı)
 * @param miad      arızanın giderilmesi için son tarih
 */
public record CreateArizaRequest(
    @NotBlank(message = "{dogrulama.bos-olamaz}")
    @Size(max = 36, message = "{dogrulama.en-fazla}")
    String bildirenSicil,

    @NotBlank(message = "{dogrulama.bos-olamaz}")
    String sebep,

    @NotNull(message = "{dogrulama.zorunlu}")
    BigDecimal asamaKodu,

    @NotNull(message = "{dogrulama.zorunlu}")
    LocalDateTime tarih,

    LocalDateTime miad,

    LocalDateTime teslimTarihi,

    @Size(max = 15, message = "{dogrulama.en-fazla}")
    String iletisimTelefonu,

    Long envanterId,

    Long depoId
) {
}
