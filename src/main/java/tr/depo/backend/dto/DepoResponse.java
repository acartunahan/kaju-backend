package tr.depo.backend.dto;

import java.time.LocalDateTime;

/** Depo cevabı. Audit zamanları UTC'dir. */
public record DepoResponse(
    Long id,
    String ad,
    String aciklama,
    String durumKodu,
    LocalDateTime kayitTarihi,
    String sorumluSicil,
    int versiyon,
    LocalDateTime olusturmaZamani,
    String olusturan,
    LocalDateTime guncellemeZamani,
    String guncelleyen
) {
}
