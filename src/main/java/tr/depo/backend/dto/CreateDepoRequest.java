package tr.depo.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Yeni depo. Sınırlar DEPO tablosuyla aynıdır.
 *
 * @param durumKodu    legacy ham kod (anlamı doğrulanmadı), en fazla 1 karakter
 * @param sorumluSicil sorumlu personelin sicili; yalnızca trim edilir
 */
public record CreateDepoRequest(
    @NotBlank(message = "{dogrulama.bos-olamaz}")
    @Size(max = 50, message = "{dogrulama.en-fazla}")
    String ad,

    @Size(max = 200, message = "{dogrulama.en-fazla}")
    String aciklama,

    @Size(max = 1, message = "{dogrulama.en-fazla}")
    String durumKodu,

    LocalDateTime kayitTarihi,

    @Size(max = 10, message = "{dogrulama.en-fazla}")
    String sorumluSicil
) {
}
