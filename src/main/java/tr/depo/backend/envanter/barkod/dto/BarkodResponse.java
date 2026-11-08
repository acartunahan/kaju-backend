package tr.depo.backend.envanter.barkod.dto;

import java.time.LocalDateTime;

public record BarkodResponse(
    Long id,
    Long envanterId,
    String barkod,
    boolean seriNoMu,
    int versiyon,
    LocalDateTime olusturmaZamani,
    String olusturan,
    LocalDateTime guncellemeZamani,
    String guncelleyen
) {
}
