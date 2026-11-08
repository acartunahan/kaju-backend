package tr.depo.backend.dto;

import java.time.LocalDateTime;

public record EnvanterNotuResponse(
    Long id,
    Long envanterId,
    String notMetni,
    String zimmetSahibiMetni,
    LocalDateTime kayitTarihi,
    String ekleyenAdi,
    String ekleyenSicil,
    int versiyon,
    LocalDateTime olusturmaZamani,
    String olusturan,
    LocalDateTime guncellemeZamani,
    String guncelleyen
) {
}
