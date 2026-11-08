package tr.depo.backend.dto;

import java.time.LocalDateTime;

public record EnvanterAparatiResponse(
    Long id,
    Long envanterId,
    String ad,
    int adet,
    String seriNo,
    String aciklama,
    int versiyon,
    LocalDateTime olusturmaZamani,
    String olusturan,
    LocalDateTime guncellemeZamani,
    String guncelleyen
) {
}
