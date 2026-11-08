package tr.depo.backend.envanter.demirbas.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import tr.depo.backend.common.mapping.IliskiOzeti;
import tr.depo.domain.envanter.AlimYolu;
import tr.depo.domain.envanter.DemirbasDurumu;

/**
 * Demirbaş cevabı. İlişkiler silinmişse {@code null}. Göçle gelen kayıtlarda {@code kayitTarihi} 1899-12-30 olabilir:
 * legacy'deki Delphi boş tarihidir, "bilinmiyor" anlamına gelir.
 */
public record DemirbasResponse(
    Long id,
    DemirbasDurumu durum,
    LocalDateTime kayitTarihi,
    Long demirbasNo,
    String seriNo,
    String ad,
    String aciklama,
    AlimYolu alimYolu,
    BigDecimal birimKodu,
    String saymanlikNo,
    String zimmetliSicil,
    String ilSubeDaireBskNo,
    String alimTuruKodu,
    LocalDateTime garantiBaslangic,
    LocalDateTime garantiBitis,
    IliskiOzeti kategori,
    IliskiOzeti marka,
    IliskiOzeti model,
    IliskiOzeti depo,
    IliskiOzeti ihale,
    int versiyon,
    LocalDateTime olusturmaZamani,
    String olusturan,
    LocalDateTime guncellemeZamani,
    String guncelleyen
) {
}
