package tr.depo.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import tr.depo.domain.sayim.SayimIslemi;
import tr.depo.domain.sayim.SayimKapsami;
import tr.depo.domain.sayim.YerelSayimTuru;

/**
 * Sayıma satır. Satır türü (YEREL / GENEL) sonradan değiştirilemez. Türe göre (domain kuralları, 422):
 * <ul>
 *   <li>YEREL: {@code malzemeEskiRef}, {@code zimmetEskiRef}, {@code malzemeTuru} zorunlu; {@code islem} SAYILMADI,
 *       SAYILDI, ZIMMET_DUZELTME ya da REZERVE; envantere yalnızca ZIMMET satırı bağlanır.</li>
 *   <li>GENEL: {@code islemTarihi} zorunlu; {@code islem} SAYILMADI, SAYILDI ya da DUZELTME. Anlık malzeme bilgisi,
 *       depo, açıklama ve merkez notu bu türe aittir.</li>
 * </ul>
 */
public record CreateSayimSatiriRequest(
    @NotNull(message = "{dogrulama.zorunlu}")
    SayimKapsami tur,

    @NotNull(message = "{dogrulama.zorunlu}")
    SayimIslemi islem,

    LocalDateTime islemTarihi,

    YerelSayimTuru malzemeTuru,

    @Size(max = 36, message = "{dogrulama.en-fazla}")
    String malzemeEskiRef,

    @Size(max = 36, message = "{dogrulama.en-fazla}")
    String zimmetEskiRef,

    Long envanterId,

    Long depoId,

    @Size(max = 75, message = "{dogrulama.en-fazla}")
    String anlikAd,

    @Size(max = 50, message = "{dogrulama.en-fazla}")
    String anlikMarka,

    @Size(max = 50, message = "{dogrulama.en-fazla}")
    String anlikModel,

    @Size(max = 50, message = "{dogrulama.en-fazla}")
    String anlikSeriNo,

    @Size(max = 150, message = "{dogrulama.en-fazla}")
    String aciklama,

    @Size(max = 150, message = "{dogrulama.en-fazla}")
    String merkezNotu
) {
}
