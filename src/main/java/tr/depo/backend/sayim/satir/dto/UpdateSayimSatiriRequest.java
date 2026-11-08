package tr.depo.backend.sayim.satir.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import tr.depo.domain.sayim.SayimIslemi;
import tr.depo.domain.sayim.YerelSayimTuru;

/**
 * Sayım satırı tam güncellemesi (PUT). {@code tur} değiştirilemez; türe göre kurallar oluşturmadakiyle aynıdır.
 *
 * @param versiyon istemcinin okuduğu sürüm; güncel değilse 409
 */
public record UpdateSayimSatiriRequest(
    @NotNull(message = "{dogrulama.zorunlu}")
    @PositiveOrZero(message = "{dogrulama.sifir-veya-pozitif}")
    Integer versiyon,

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
