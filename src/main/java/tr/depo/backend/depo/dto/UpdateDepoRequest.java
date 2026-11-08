package tr.depo.backend.depo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Depo tam güncellemesi (PUT): gönderilmeyen isteğe bağlı alanlar boşaltılır.
 *
 * @param versiyon istemcinin okuduğu sürüm; güncel değilse 409
 */
public record UpdateDepoRequest(
    @NotNull(message = "{dogrulama.zorunlu}")
    @PositiveOrZero(message = "{dogrulama.sifir-veya-pozitif}")
    Integer versiyon,

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
