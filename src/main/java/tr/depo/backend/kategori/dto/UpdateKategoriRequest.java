package tr.depo.backend.kategori.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Kategori tam güncellemesi (PUT). {@code tur} değiştirilemez. Üst kategori değişirse ağaçta döngü oluşmamalı.
 *
 * @param versiyon istemcinin okuduğu sürüm; güncel değilse 409
 */
public record UpdateKategoriRequest(
    @NotNull(message = "{dogrulama.zorunlu}")
    @PositiveOrZero(message = "{dogrulama.sifir-veya-pozitif}")
    Integer versiyon,

    Long ustKategoriId,

    @NotBlank(message = "{dogrulama.bos-olamaz}")
    @Size(max = 120, message = "{dogrulama.en-fazla}")
    String ad,

    @Size(max = 100, message = "{dogrulama.en-fazla}")
    String yorum,

    @Size(max = 1, message = "{dogrulama.en-fazla}")
    String durumKodu,

    LocalDateTime kayitTarihi,

    Long depoId
) {
}
