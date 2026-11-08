package tr.depo.backend.model.dto;

import java.time.LocalDateTime;
import tr.depo.backend.common.mapping.IliskiOzeti;
import tr.depo.domain.tanim.MalzemeTuru;

/** Model cevabı. İlişkiler silinmişse {@code null}. */
public record ModelResponse(
    Long id,
    MalzemeTuru tur,
    IliskiOzeti marka,
    IliskiOzeti kategori,
    String ad,
    String aciklama,
    String durumKodu,
    LocalDateTime kayitTarihi,
    Boolean aktif,
    IliskiOzeti depo,
    int versiyon,
    LocalDateTime olusturmaZamani,
    String olusturan,
    LocalDateTime guncellemeZamani,
    String guncelleyen
) {
}
