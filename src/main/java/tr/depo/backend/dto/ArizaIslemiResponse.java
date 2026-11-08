package tr.depo.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import tr.depo.domain.ariza.ArizaIslemTuru;

public record ArizaIslemiResponse(
    Long id,
    Long arizaId,
    ArizaIslemTuru tur,
    BigDecimal islemKodu,
    String islemYapanSicil,
    String gorevliSicil,
    LocalDateTime zaman,
    LocalDateTime miad,
    String aciklama,
    Long siraNo,
    int versiyon,
    LocalDateTime olusturmaZamani,
    String olusturan,
    LocalDateTime guncellemeZamani,
    String guncelleyen
) {
}
