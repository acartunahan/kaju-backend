package tr.depo.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import tr.depo.domain.tanim.MalzemeTuru;

/**
 * Yeni model. Sınırlar MODEL tablosuyla aynıdır. Marka ve kategori modelle aynı türde olmalı.
 * SARF modelinde {@code markaId} zorunludur; {@code aktif} boş bırakılırsa {@code true} olur (CK_MODEL_SARF).
 *
 * @param kategoriId katalog kategorisi (isteğe bağlı)
 */
public record CreateModelRequest(
    @NotNull(message = "{dogrulama.zorunlu}")
    MalzemeTuru tur,

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
