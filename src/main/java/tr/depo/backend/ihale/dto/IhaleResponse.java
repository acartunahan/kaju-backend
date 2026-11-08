package tr.depo.backend.ihale.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import tr.depo.backend.common.mapping.IliskiOzeti;

/** İhale cevabı. {@code firma} silinmişse {@code null}. */
public record IhaleResponse(
    Long id,
    String ad,
    LocalDateTime ihaleTarihi,
    LocalDateTime kabulTarihi,
    LocalDateTime sozlesmeTarihi,
    BigDecimal birimKodu,
    String personelMetni,
    IliskiOzeti firma,
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
