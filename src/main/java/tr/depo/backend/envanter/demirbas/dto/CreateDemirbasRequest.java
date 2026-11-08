package tr.depo.backend.envanter.demirbas.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import tr.depo.domain.envanter.AlimYolu;
import tr.depo.domain.envanter.DemirbasDurumu;

/**
 * Yeni demirbaş (ENVANTER, TUR = DEMIRBAS). Sınırlar ENVANTER tablosuyla aynıdır. Kategori, marka ve model DEMIRBAS
 * türünde olmalı. {@code birimKodu}, {@code alimTuruKodu}, {@code ilSubeDaireBskNo} legacy ham değerlerdir.
 *
 * @param durum       zorunlu (domain)
 * @param kayitTarihi zorunlu (domain)
 */
public record CreateDemirbasRequest(
    @NotNull(message = "{dogrulama.zorunlu}")
    DemirbasDurumu durum,

    @NotNull(message = "{dogrulama.zorunlu}")
    LocalDateTime kayitTarihi,

    Long demirbasNo,

    @Size(max = 50, message = "{dogrulama.en-fazla}")
    String seriNo,

    @Size(max = 100, message = "{dogrulama.en-fazla}")
    String ad,

    @Size(max = 200, message = "{dogrulama.en-fazla}")
    String aciklama,

    AlimYolu alimYolu,

    BigDecimal birimKodu,

    @Size(max = 50, message = "{dogrulama.en-fazla}")
    String saymanlikNo,

    @Size(max = 7, message = "{dogrulama.en-fazla}")
    String zimmetliSicil,

    @Size(max = 36, message = "{dogrulama.en-fazla}")
    String ilSubeDaireBskNo,

    @Size(max = 1, message = "{dogrulama.en-fazla}")
    String alimTuruKodu,

    LocalDateTime garantiBaslangic,

    LocalDateTime garantiBitis,

    Long kategoriId,

    Long markaId,

    Long modelId,

    Long depoId,

    Long ihaleId
) {
}
