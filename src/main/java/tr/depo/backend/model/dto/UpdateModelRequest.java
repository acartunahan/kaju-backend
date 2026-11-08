package tr.depo.backend.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Model tam güncellemesi (PUT). {@code tur} değiştirilemez; SARF modelinde marka ve aktif boşaltılamaz.
 *
 * @param versiyon istemcinin okuduğu sürüm; güncel değilse 409
 */
public record UpdateModelRequest(
    @NotNull(message = "{dogrulama.zorunlu}")
    @PositiveOrZero(message = "{dogrulama.sifir-veya-pozitif}")
    Integer versiyon,

    Long markaId,

    Long kategoriId,

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
