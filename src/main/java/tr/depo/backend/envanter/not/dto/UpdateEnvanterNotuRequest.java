package tr.depo.backend.envanter.not.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Not tam güncellemesi (PUT).
 *
 * @param versiyon istemcinin okuduğu sürüm; güncel değilse 409
 */
public record UpdateEnvanterNotuRequest(
    @NotNull(message = "{dogrulama.zorunlu}")
    @PositiveOrZero(message = "{dogrulama.sifir-veya-pozitif}")
    Integer versiyon,

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
