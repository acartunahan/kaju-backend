package tr.depo.backend.ariza.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import tr.depo.backend.common.mapping.IliskiOzeti;

/** Arıza cevabı. İlişkiler silinmişse {@code null}. */
public record ArizaResponse(
    Long id,
    String bildirenSicil,
    String sebep,
    BigDecimal asamaKodu,
    LocalDateTime tarih,
    LocalDateTime miad,
    LocalDateTime teslimTarihi,
    String iletisimTelefonu,
    IliskiOzeti envanter,
    IliskiOzeti depo,
    int versiyon,
    LocalDateTime olusturmaZamani,
    String olusturan,
    LocalDateTime guncellemeZamani,
    String guncelleyen
) {
}
