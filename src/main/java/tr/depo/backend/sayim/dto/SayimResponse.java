package tr.depo.backend.sayim.dto;

import java.time.LocalDateTime;
import tr.depo.backend.common.mapping.IliskiOzeti;
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
    IliskiOzeti depo,
    int versiyon,
    LocalDateTime olusturmaZamani,
    String olusturan,
    LocalDateTime guncellemeZamani,
    String guncelleyen
) {
}
