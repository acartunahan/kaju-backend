package tr.depo.backend.marka.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Marka tam güncellemesi (PUT): gönderilmeyen isteğe bağlı alanlar boşaltılır. {@code tur} değiştirilemediği için
 * burada yoktur.
 *
 * @param versiyon istemcinin okuduğu sürüm; güncel değilse 409 (optimistic locking)
 * @param aktif    sarf markalarında zorunlu
 */
public record UpdateMarkaRequest(
    @NotNull(message = "{dogrulama.zorunlu}")
    @PositiveOrZero(message = "{dogrulama.sifir-veya-pozitif}")
    Integer versiyon,

    @NotBlank(message = "{dogrulama.bos-olamaz}")
    @Size(max = 255, message = "{dogrulama.en-fazla}")
    String ad,

    @Size(max = 200, message = "{dogrulama.en-fazla}")
    String aciklama,

    @Size(max = 1, message = "{dogrulama.en-fazla}")
    String durumKodu,

    LocalDateTime kayitTarihi,

    Boolean aktif,

    Long depoId
) {
}
