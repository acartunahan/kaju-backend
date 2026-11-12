package tr.depo.backend.dto;

import tr.depo.domain.tanim.MalzemeTuru;

/** Türe göre isteğe bağlı filtre (firma listesi, kategori ağacı); boşsa iki tür de. */
public record TurFiltreRequest(MalzemeTuru tur) {
}
