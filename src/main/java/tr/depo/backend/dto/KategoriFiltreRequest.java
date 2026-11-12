package tr.depo.backend.dto;

import jakarta.validation.constraints.Size;
import tr.depo.domain.tanim.MalzemeTuru;

/**
 * Kategori listesinin isteğe bağlı filtreleri; verilenlerin hepsi birlikte uygulanır.
 *
 * @param ad            adda geçen metin (büyük/küçük harf duyarsız)
 * @param ustKategoriId bu kategorinin doğrudan alt kategorileri
 */
public record KategoriFiltreRequest(
    MalzemeTuru tur,
    @Size(max = 100, message = "{dogrulama.en-fazla}") String ad,
    Long ustKategoriId
) {
}
