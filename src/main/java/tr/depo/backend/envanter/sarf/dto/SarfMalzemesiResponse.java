package tr.depo.backend.envanter.sarf.dto;

import java.time.LocalDateTime;
import tr.depo.backend.common.mapping.IliskiOzeti;

/** Sarf malzemesi cevabı. İlişkiler silinmişse {@code null}. */
public record SarfMalzemesiResponse(
    Long id,
    String ad,
    String aciklama,
    String durumKodu,
    LocalDateTime kayitTarihi,
    IliskiOzeti kategori,
    IliskiOzeti marka,
    IliskiOzeti model,
    IliskiOzeti depo,
    int versiyon,
    LocalDateTime olusturmaZamani,
    String olusturan,
    LocalDateTime guncellemeZamani,
    String guncelleyen
) {
}
