package tr.depo.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * Sarf malzemesi listesinin isteğe bağlı filtreleri (sorgu parametreleri); verilenlerin hepsi birlikte uygulanır.
 *
 * @param altKategoriler kategorinin alt kategorilerindekiler de dahil mi (varsayılan: evet)
 * @param q              ad, açıklama ve barkodda arama (büyük/küçük harf duyarsız)
 */
public record SarfMalzemesiFiltreRequest(
    Long kategoriId,
    @Schema(defaultValue = "true") Boolean altKategoriler,
    Long markaId,
    Long modelId,
    Long depoId,
    @Size(max = 100, message = "{dogrulama.en-fazla}") String q
) {
}
