package tr.depo.backend.hareket.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import tr.depo.domain.tanim.MalzemeTuru;

/**
 * Yeni envanter hareketi (geçmiş kaydı; sonradan değiştirilemez ve silinemez). Sınırlar ENVANTER_HAREKETI tablosuyla
 * aynıdır. Envanter hareketle aynı türde olmalı (422).
 * <ul>
 *   <li>DEMIRBAS (transfer, zimmet…): {@code hareketNoKodu} zorunlu (domain); nereden/nereye metin ve/veya depo,
 *       işlemi yapan sicil.</li>
 *   <li>SARF (stok giriş/çıkış): miktar, yön kodu, seri no, alım tarihi, alan depo.</li>
 * </ul>
 * {@code hareketNoKodu}, {@code yonKodu} ve {@code durumKodu} legacy ham değerlerdir; anlamları doğrulanmadı.
 */
public record CreateEnvanterHareketiRequest(
    @NotNull(message = "{dogrulama.zorunlu}")
    MalzemeTuru tur,

    Long envanterId,

    @Size(max = 1000, message = "{dogrulama.en-fazla}")
    String aciklama,

    LocalDateTime kayitTarihi,

    @Size(max = 50, message = "{dogrulama.en-fazla}")
    String hareketAdi,

    @Size(max = 200, message = "{dogrulama.en-fazla}")
    String neredenMetni,

    Long neredenDepoId,

    @Size(max = 200, message = "{dogrulama.en-fazla}")
    String nereyeMetni,

    Long nereyeDepoId,

    @Size(max = 6, message = "{dogrulama.en-fazla}")
    String islemYapanSicil,

    BigDecimal hareketNoKodu,

    BigDecimal miktar,

    @Size(max = 36, message = "{dogrulama.en-fazla}")
    String yonKodu,

    @Size(max = 200, message = "{dogrulama.en-fazla}")
    String seriNo,

    LocalDateTime alimTarihi,

    @Size(max = 1, message = "{dogrulama.en-fazla}")
    String durumKodu,

    Long alanDepoId
) {
}
