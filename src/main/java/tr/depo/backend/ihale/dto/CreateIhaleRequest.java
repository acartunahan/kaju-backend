package tr.depo.backend.ihale.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Yeni ihale. Sınırlar IHALE tablosuyla aynıdır. Firma yalnızca DEMIRBAS türünde olabilir.
 * Kod alanları ({@code birimKodu}, {@code yerindeGarantiKodu}, {@code onarimSuresiKodu}, {@code durumKodu}) legacy ham
 * değerlerdir; anlamları doğrulanmadı.
 */
public record CreateIhaleRequest(
    @Size(max = 50, message = "{dogrulama.en-fazla}")
    String ad,

    LocalDateTime ihaleTarihi,

    LocalDateTime kabulTarihi,

    LocalDateTime sozlesmeTarihi,

    BigDecimal birimKodu,

    @Size(max = 50, message = "{dogrulama.en-fazla}")
    String personelMetni,

    Long firmaId,

    @Size(max = 200, message = "{dogrulama.en-fazla}")
    String aciklama,

    @NotBlank(message = "{dogrulama.bos-olamaz}")
    @Size(max = 1, message = "{dogrulama.en-fazla}")
    String durumKodu,

    @NotNull(message = "{dogrulama.zorunlu}")
    LocalDateTime kayitTarihi,

    @Size(max = 100, message = "{dogrulama.en-fazla}")
    String komisyonBaskani,

    @Size(max = 100, message = "{dogrulama.en-fazla}")
    String komisyonUye1,

    @Size(max = 100, message = "{dogrulama.en-fazla}")
    String komisyonUye2,

    BigDecimal yerindeGarantiKodu,

    BigDecimal onarimSuresiKodu
) {
}
