package tr.depo.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import tr.depo.domain.envanter.DemirbasDurumu;

/**
 * Demirbaş listesinin isteğe bağlı filtreleri (sorgu parametreleri); verilenlerin hepsi birlikte uygulanır.
 *
 * @param kategoriId     o kategorideki demirbaşlar
 * @param altKategoriler kategorinin alt kategorilerindekiler de dahil mi (varsayılan: evet)
 * @param zimmetliSicil  o sicile şu an zimmetli olanlar (tam eşleşme; boş verilirse 400)
 * @param zimmetli       true: birine zimmetli olanlar; false: kimseye zimmetli olmayanlar
 * @param q              ad, seri no, demirbaş no, açıklama ve barkodda arama (büyük/küçük harf duyarsız)
 */
public record DemirbasFiltreRequest(
    Long kategoriId,
    @Schema(defaultValue = "true") Boolean altKategoriler,
    Long markaId,
    Long modelId,
    Long depoId,
    Long ihaleId,
    DemirbasDurumu durum,
    String zimmetliSicil,
    Boolean zimmetli,
    @Size(max = 100, message = "{dogrulama.en-fazla}") String q
) {
}
