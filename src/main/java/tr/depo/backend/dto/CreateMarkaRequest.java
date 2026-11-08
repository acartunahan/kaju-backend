package tr.depo.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import tr.depo.domain.tanim.MalzemeTuru;

/**
 * Yeni marka. Sınırlar domain kolonlarıyla aynıdır (MARKA tablosu).
 *
 * @param tur        DEMIRBAS ya da SARF; sonradan değiştirilemez
 * @param aktif      yalnızca sarf markalarında anlamlı; sarfta boş bırakılırsa {@code true}
 * @param durumKodu  legacy ham kod (anlamı doğrulanmadı), en fazla 1 karakter
 * @param depoId     isteğe bağlı; verilirse aktif bir depo olmalı
 */
public record CreateMarkaRequest(
    @NotNull(message = "{dogrulama.zorunlu}")
    MalzemeTuru tur,

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
