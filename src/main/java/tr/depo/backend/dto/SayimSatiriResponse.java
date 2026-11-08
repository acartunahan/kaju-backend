package tr.depo.backend.dto;

import java.time.LocalDateTime;
import tr.depo.domain.sayim.SayimIslemi;
import tr.depo.domain.sayim.SayimKapsami;
import tr.depo.domain.sayim.YerelSayimTuru;

/** Sayım satırı cevabı. Türe özgü alanlar diğer türde boştur; ilişkiler silinmişse {@code null}. */
public record SayimSatiriResponse(
    Long id,
    Long sayimId,
    SayimKapsami tur,
    SayimIslemi islem,
    LocalDateTime islemTarihi,
    YerelSayimTuru malzemeTuru,
    String malzemeEskiRef,
    String zimmetEskiRef,
    IliskiOzetiResponse envanter,
    IliskiOzetiResponse depo,
    String anlikAd,
    String anlikMarka,
    String anlikModel,
    String anlikSeriNo,
    String aciklama,
    String merkezNotu,
    int versiyon,
    LocalDateTime olusturmaZamani,
    String olusturan,
    LocalDateTime guncellemeZamani,
    String guncelleyen
) {
}
