package tr.depo.backend.envanter.demirbas.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import tr.depo.domain.envanter.AlimYolu;
import tr.depo.domain.envanter.DemirbasDurumu;

/**
 * Demirbaş tam güncellemesi (PUT): gönderilmeyen isteğe bağlı alanlar boşaltılır.
 *
 * @param versiyon istemcinin okuduğu sürüm; güncel değilse 409
 */
public record UpdateDemirbasRequest(
    @NotNull(message = "{dogrulama.zorunlu}")
    @PositiveOrZero(message = "{dogrulama.sifir-veya-pozitif}")
    Integer versiyon,

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
