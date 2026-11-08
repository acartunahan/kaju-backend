package tr.depo.backend.envanter.aparat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Aparat tam güncellemesi (PUT).
 *
 * @param versiyon istemcinin okuduğu sürüm; güncel değilse 409
 */
public record UpdateEnvanterAparatiRequest(
    @NotNull(message = "{dogrulama.zorunlu}")
    @PositiveOrZero(message = "{dogrulama.sifir-veya-pozitif}")
    Integer versiyon,

    @NotBlank(message = "{dogrulama.bos-olamaz}")
    @Size(max = 100, message = "{dogrulama.en-fazla}")
    String ad,

    @NotNull(message = "{dogrulama.zorunlu}")
    @Positive(message = "{dogrulama.pozitif}")
    Integer adet,

    @Size(max = 50, message = "{dogrulama.en-fazla}")
    String seriNo,

    @Size(max = 500, message = "{dogrulama.en-fazla}")
    String aciklama
) {
}
