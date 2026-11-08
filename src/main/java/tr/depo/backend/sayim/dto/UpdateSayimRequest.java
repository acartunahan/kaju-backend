package tr.depo.backend.sayim.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import tr.depo.domain.sayim.SayimDurumu;
import tr.depo.domain.sayim.SayimKapsami;

/**
 * Sayım tam güncellemesi (PUT).
 *
 * @param versiyon istemcinin okuduğu sürüm; güncel değilse 409
 */
public record UpdateSayimRequest(
    @NotNull(message = "{dogrulama.zorunlu}")
    @PositiveOrZero(message = "{dogrulama.sifir-veya-pozitif}")
    Integer versiyon,

    @NotNull(message = "{dogrulama.zorunlu}")
    SayimKapsami kapsam,

    @NotBlank(message = "{dogrulama.bos-olamaz}")
    @Size(max = 60, message = "{dogrulama.en-fazla}")
    String baslik,

    LocalDateTime baslangicTarihi,

    LocalDateTime bitisTarihi,

    SayimDurumu durum,

    Long depoId
) {
}
