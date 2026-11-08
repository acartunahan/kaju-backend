package tr.depo.backend.dto;

import java.time.LocalDateTime;
import tr.depo.domain.envanter.ZimmetIslemi;

/**
 * Zimmet kaydı cevabı. {@code malzemeEskiRef} yalnızca göçle gelen kayıtlarda dolu olabilir (legacy SARF_LOG.MALZEME,
 * çözülemeyen ham referans).
 */
public record ZimmetKaydiResponse(
    Long id,
    String sicil,
    ZimmetIslemi islem,
    String bilgi,
    LocalDateTime tarih,
    String islemYapanSicil,
    IliskiOzetiResponse depo,
    String malzemeEskiRef,
    int versiyon,
    LocalDateTime olusturmaZamani,
    String olusturan
) {
}
