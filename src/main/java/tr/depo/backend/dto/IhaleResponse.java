package tr.depo.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** İhale cevabı. {@code firma} silinmişse {@code null}. */
public record IhaleResponse(
    Long id,
    String ad,
    LocalDateTime ihaleTarihi,
    LocalDateTime kabulTarihi,
    LocalDateTime sozlesmeTarihi,
    BigDecimal birimKodu,
    String personelMetni,
    IliskiOzetiResponse firma,
    String aciklama,
    String durumKodu,
    LocalDateTime kayitTarihi,
    String komisyonBaskani,
    String komisyonUye1,
    String komisyonUye2,
    BigDecimal yerindeGarantiKodu,
    BigDecimal onarimSuresiKodu,
    int versiyon,
    LocalDateTime olusturmaZamani,
    String olusturan,
    LocalDateTime guncellemeZamani,
    String guncelleyen
) {
}
