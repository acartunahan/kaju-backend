package tr.depo.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import tr.depo.domain.tanim.MalzemeTuru;

/**
 * Yeni kategori (katalog ağacı düğümü). Sınırlar KATEGORI tablosuyla aynıdır.
 *
 * @param ustKategoriId boşsa kök; verilirse aynı türde aktif bir kategori olmalı
 * @param durumKodu     DEMIRBAS kategorisinde zorunlu (domain kuralı)
 */
public record CreateKategoriRequest(
    @NotNull(message = "{dogrulama.zorunlu}")
    MalzemeTuru tur,

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
