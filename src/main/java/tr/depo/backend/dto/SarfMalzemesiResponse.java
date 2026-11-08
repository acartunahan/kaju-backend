package tr.depo.backend.dto;

import java.time.LocalDateTime;

/** Sarf malzemesi cevabı. İlişkiler silinmişse {@code null}. */
public record SarfMalzemesiResponse(
    Long id,
    String ad,
    String aciklama,
    String durumKodu,
    LocalDateTime kayitTarihi,
    IliskiOzetiResponse kategori,
    IliskiOzetiResponse marka,
    IliskiOzetiResponse model,
    IliskiOzetiResponse depo,
    int versiyon,
    LocalDateTime olusturmaZamani,
    String olusturan,
    LocalDateTime guncellemeZamani,
    String guncelleyen
) {
}
