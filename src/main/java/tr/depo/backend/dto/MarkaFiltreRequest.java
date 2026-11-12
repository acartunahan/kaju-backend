package tr.depo.backend.dto;

import jakarta.validation.constraints.Size;
import tr.depo.domain.tanim.MalzemeTuru;

/**
 * Marka listesinin isteğe bağlı filtreleri; verilenlerin hepsi birlikte uygulanır.
 *
 * @param ad    adda geçen metin (büyük/küçük harf duyarsız)
 * @param aktif yalnızca aktif (true) ya da pasif (false) olanlar
 */
public record MarkaFiltreRequest(
    MalzemeTuru tur,
    @Size(max = 100, message = "{dogrulama.en-fazla}") String ad,
    Boolean aktif
) {
}
