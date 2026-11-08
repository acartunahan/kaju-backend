package tr.depo.backend.hareket.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import tr.depo.backend.common.mapping.IliskiOzeti;
import tr.depo.domain.tanim.MalzemeTuru;

/**
 * Envanter hareketi cevabı. Türe özgü alanlar diğer türde boştur. {@code markaEskiRef}, {@code modelEskiRef},
 * {@code firmaEskiRef} yalnızca göçle gelen sarf hareketlerinde dolu olabilir (legacy'de çözülemeyen ham referans).
 */
public record EnvanterHareketiResponse(
    Long id,
    MalzemeTuru tur,
    IliskiOzeti envanter,
    String aciklama,
    LocalDateTime kayitTarihi,
    String hareketAdi,
    String neredenMetni,
    IliskiOzeti neredenDepo,
    String nereyeMetni,
    IliskiOzeti nereyeDepo,
    String islemYapanSicil,
    BigDecimal hareketNoKodu,
    BigDecimal miktar,
    String yonKodu,
    String seriNo,
    LocalDateTime alimTarihi,
    String durumKodu,
    IliskiOzeti alanDepo,
    String markaEskiRef,
    String modelEskiRef,
    String firmaEskiRef,
    int versiyon,
    LocalDateTime olusturmaZamani,
    String olusturan
) {
}
