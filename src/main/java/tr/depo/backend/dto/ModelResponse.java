package tr.depo.backend.dto;

import java.time.LocalDateTime;
import tr.depo.domain.tanim.MalzemeTuru;

/** Model cevabı. İlişkiler silinmişse {@code null}. */
public record ModelResponse(
    Long id,
    MalzemeTuru tur,
    IliskiOzetiResponse marka,
    IliskiOzetiResponse kategori,
    String ad,
    String aciklama,
    String durumKodu,
    LocalDateTime kayitTarihi,
    Boolean aktif,
    IliskiOzetiResponse depo,
    int versiyon,
    LocalDateTime olusturmaZamani,
    String olusturan,
    LocalDateTime guncellemeZamani,
    String guncelleyen
) {
}
