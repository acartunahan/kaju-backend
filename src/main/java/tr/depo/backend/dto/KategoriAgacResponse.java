package tr.depo.backend.dto;

import java.util.List;
import tr.depo.domain.tanim.MalzemeTuru;

/** Kategori ağacındaki bir düğüm; alt kategoriler ada göre sıralı. */
public record KategoriAgacResponse(
    Long id,
    MalzemeTuru tur,
    String ad,
    List<KategoriAgacResponse> altKategoriler
) {
}
