package tr.depo.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    IliskiOzetiResponse kategori,
    IliskiOzetiResponse marka,
    IliskiOzetiResponse model,
    IliskiOzetiResponse depo,
    IliskiOzetiResponse ihale,
    int versiyon,
    LocalDateTime olusturmaZamani,
    String olusturan,
    LocalDateTime guncellemeZamani,
    String guncelleyen
) {
}
