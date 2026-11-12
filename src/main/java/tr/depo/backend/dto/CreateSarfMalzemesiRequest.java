package tr.depo.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Yeni sarf malzemesi (ENVANTER, TUR = SARF; ör. Toner, A4 kağıt). Legacy R_TUKETIMCINSLERI'nin alanları ve sarf
 * tanımlarına bağlantılar. Kategori, marka ve model zorunlu ve SARF türünde olmalı; model seçilen markaya ait olmalı. Stok miktarı tutulmaz (hareketlerden gelir).
 *
 * @param durumKodu legacy ham kod (anlamı doğrulanmadı)
 */
public record CreateSarfMalzemesiRequest(
    @NotBlank(message = "{dogrulama.bos-olamaz}")
    @Size(max = 100, message = "{dogrulama.en-fazla}")
    String ad,

    @Size(max = 200, message = "{dogrulama.en-fazla}")
    String aciklama,

    @Size(max = 1, message = "{dogrulama.en-fazla}")
    String durumKodu,

    LocalDateTime kayitTarihi,

    @NotNull(message = "{dogrulama.zorunlu}")
    Long kategoriId,

    @NotNull(message = "{dogrulama.zorunlu}")
    Long markaId,

    @NotNull(message = "{dogrulama.zorunlu}")
    Long modelId,

    Long depoId
) {
}
