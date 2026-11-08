package tr.depo.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    IliskiOzetiResponse envanter,
    IliskiOzetiResponse depo,
    int versiyon,
    LocalDateTime olusturmaZamani,
    String olusturan,
    LocalDateTime guncellemeZamani,
    String guncelleyen
) {
}
