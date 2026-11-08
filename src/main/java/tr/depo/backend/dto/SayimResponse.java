package tr.depo.backend.dto;

import java.time.LocalDateTime;
import tr.depo.domain.sayim.SayimDurumu;
import tr.depo.domain.sayim.SayimKapsami;

/** Sayım cevabı. {@code durum} göçle gelen kayıtlarda boş olabilir. */
public record SayimResponse(
    Long id,
    SayimKapsami kapsam,
    String baslik,
    LocalDateTime baslangicTarihi,
    LocalDateTime bitisTarihi,
    SayimDurumu durum,
    IliskiOzetiResponse depo,
    int versiyon,
    LocalDateTime olusturmaZamani,
    String olusturan,
    LocalDateTime guncellemeZamani,
    String guncelleyen
) {
}
