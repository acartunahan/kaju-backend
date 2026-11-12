package tr.depo.backend.dto;

import java.time.LocalDateTime;
import tr.depo.domain.envanter.ZimmetGecmisiKaynagi;

/**
 * Demirbaşın zimmet geçmişindeki bir değişiklik.
 *
 * @param oncekiSicil   değişiklikten önceki sicil; ilk zimmette ve migration başlangıç kaydında boş
 * @param yeniSicil     değişiklikten sonraki sicil; zimmet kaldırıldıysa boş
 * @param degisimZamani değişikliğin zamanı (UTC). {@code MIGRATION_BASLANGICI} kaydında migration anıdır; gerçek
 *                      zimmet tarihi bilinmez
 * @param olusturan     değişikliği yapan kullanıcı ({@code MIGRATION}: migration'ın yazdığı başlangıç kaydı)
 */
public record ZimmetGecmisiResponse(
    Long id,
    Long demirbasId,
    String oncekiSicil,
    String yeniSicil,
    LocalDateTime degisimZamani,
    ZimmetGecmisiKaynagi kaynak,
    LocalDateTime olusturmaZamani,
    String olusturan
) {
}
