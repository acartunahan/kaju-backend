package tr.depo.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Firma tam güncellemesi (PUT). {@code tur} değiştirilemez. Türe göre zorunluluklar oluşturmadakiyle aynıdır.
 *
 * @param versiyon istemcinin okuduğu sürüm; güncel değilse 409
 */
public record UpdateFirmaRequest(
    @NotNull(message = "{dogrulama.zorunlu}")
    @PositiveOrZero(message = "{dogrulama.sifir-veya-pozitif}")
    Integer versiyon,

    @Size(max = 255, message = "{dogrulama.en-fazla}")
    String ad,

    @Size(max = 255, message = "{dogrulama.en-fazla}")
    String yetkiliAd,

    @Size(max = 50, message = "{dogrulama.en-fazla}")
    String yetkiliTelefon,

    @Size(max = 255, message = "{dogrulama.en-fazla}")
    String irtibatAd,

    @Size(max = 255, message = "{dogrulama.en-fazla}")
    String irtibatTelefon,

    @Size(max = 50, message = "{dogrulama.en-fazla}")
    String telefon1,

    @Size(max = 50, message = "{dogrulama.en-fazla}")
    String telefon2,

    @Size(max = 50, message = "{dogrulama.en-fazla}")
    String faks,

    @Size(max = 255, message = "{dogrulama.en-fazla}")
    String webAdresi,

    @Size(max = 255, message = "{dogrulama.en-fazla}")
    String adres,

    @Size(max = 300, message = "{dogrulama.en-fazla}")
    String notlar,

    @Size(max = 1, message = "{dogrulama.en-fazla}")
    String durumKodu,

    LocalDateTime kayitTarihi,

    Long depoId
) {
}
