package tr.depo.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import tr.depo.domain.envanter.ZimmetIslemi;

/**
 * Yeni sarf zimmet/düşüm kaydı (geçmiş kaydı; sonradan değiştirilemez ve silinemez). Sınırlar ZIMMET_KAYDI tablosuyla
 * aynıdır. Legacy {@code malzemeEskiRef} neye işaret ettiği doğrulanmadığı için yeni kayıtlarda yazılmaz.
 *
 * @param sicil zimmetlenen / düşüm yapılan personelin sicili; yalnızca trim edilir
 */
public record CreateZimmetKaydiRequest(
    @NotBlank(message = "{dogrulama.bos-olamaz}")
    @Size(max = 7, message = "{dogrulama.en-fazla}")
    String sicil,

    @NotNull(message = "{dogrulama.zorunlu}")
    ZimmetIslemi islem,

    @Size(max = 100, message = "{dogrulama.en-fazla}")
    String bilgi,

    LocalDateTime tarih,

    @Size(max = 36, message = "{dogrulama.en-fazla}")
    String islemYapanSicil,

    Long depoId
) {
}
