package tr.depo.backend.firma.dto;

import java.time.LocalDateTime;
import tr.depo.backend.common.mapping.IliskiOzeti;
import tr.depo.domain.tanim.MalzemeTuru;

/** Firma cevabı. {@code depo} silinmişse {@code null}. */
public record FirmaResponse(
    Long id,
    MalzemeTuru tur,
    String ad,
    String yetkiliAd,
    String yetkiliTelefon,
    String irtibatAd,
    String irtibatTelefon,
    String telefon1,
    String telefon2,
    String faks,
    String webAdresi,
    String adres,
    String notlar,
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
