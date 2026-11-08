package tr.depo.backend.marka.dto;

import java.time.LocalDateTime;
import tr.depo.backend.common.mapping.IliskiOzeti;
import tr.depo.domain.tanim.MalzemeTuru;

/**
 * Marka cevabı. Audit zamanları UTC'dir. {@code depo} silinmişse {@code null}.
 *
 * @param versiyon PUT isteğinde geri gönderilmesi gereken sürüm
 */
public record MarkaResponse(
    Long id,
    MalzemeTuru tur,
    String ad,
    String aciklama,
    String durumKodu,
    LocalDateTime kayitTarihi,
    Boolean aktif,
    IliskiOzeti depo,
    int versiyon,
    LocalDateTime olusturmaZamani,
    String olusturan,
    LocalDateTime guncellemeZamani,
    String guncelleyen
) {
}
