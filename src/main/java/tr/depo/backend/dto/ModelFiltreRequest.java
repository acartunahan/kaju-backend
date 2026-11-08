package tr.depo.backend.dto;

import jakarta.validation.constraints.Size;
import tr.depo.domain.tanim.MalzemeTuru;

/**
 * Model listesinin isteğe bağlı filtreleri; verilenlerin hepsi birlikte uygulanır.
 *
 * @param markaId seçilen markanın modelleri (ürün formunda marka seçildikten sonra)
 * @param ad      adda geçen metin (büyük/küçük harf duyarsız)
 * @param aktif   yalnızca aktif (true) ya da pasif (false) olanlar
 */
public record ModelFiltreRequest(
    MalzemeTuru tur,
    Long markaId,
    Long kategoriId,
    @Size(max = 100, message = "{dogrulama.en-fazla}") String ad,
    Boolean aktif
) {
}
