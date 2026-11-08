package tr.depo.backend.kategori.dto;

import java.time.LocalDateTime;
import tr.depo.backend.common.mapping.IliskiOzeti;
import tr.depo.domain.tanim.MalzemeTuru;

/** Kategori cevabı. {@code ust} kökte ya da üst kategori silinmişse {@code null}. */
public record KategoriResponse(
    Long id,
    MalzemeTuru tur,
    IliskiOzeti ust,
    String ad,
    String yorum,
    String durumKodu,
    LocalDateTime kayitTarihi,
    IliskiOzeti depo,
    int versiyon,
    LocalDateTime olusturmaZamani,
    String olusturan,
    LocalDateTime guncellemeZamani,
    String guncelleyen
) {
}
