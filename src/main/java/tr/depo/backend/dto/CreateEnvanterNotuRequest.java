package tr.depo.backend.dto;

import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Envantere not. Sınırlar ENVANTER_NOTU tablosuyla aynıdır; domain'de zorunlu alan yoktur.
 *
 * @param ekleyenAdi   notu ekleyenin adı (o anki değerin kopyası)
 * @param ekleyenSicil notu ekleyenin sicili; yalnızca trim edilir
 */
public record CreateEnvanterNotuRequest(
    @Size(max = 1000, message = "{dogrulama.en-fazla}")
    String notMetni,

    @Size(max = 500, message = "{dogrulama.en-fazla}")
    String zimmetSahibiMetni,

    LocalDateTime kayitTarihi,

    @Size(max = 123, message = "{dogrulama.en-fazla}")
    String ekleyenAdi,

    @Size(max = 7, message = "{dogrulama.en-fazla}")
    String ekleyenSicil
) {
}
