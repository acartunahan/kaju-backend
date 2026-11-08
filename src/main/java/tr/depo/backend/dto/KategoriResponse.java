package tr.depo.backend.dto;

import java.time.LocalDateTime;
import tr.depo.domain.tanim.MalzemeTuru;

/** Kategori cevabı. {@code ust} kökte ya da üst kategori silinmişse {@code null}. */
public record KategoriResponse(
    Long id,
    MalzemeTuru tur,
    IliskiOzetiResponse ust,
    String ad,
    String yorum,
    String durumKodu,
    LocalDateTime kayitTarihi,
    IliskiOzetiResponse depo,
    int versiyon,
    LocalDateTime olusturmaZamani,
    String olusturan,
    LocalDateTime guncellemeZamani,
    String guncelleyen
) {
}
