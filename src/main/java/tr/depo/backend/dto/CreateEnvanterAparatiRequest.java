package tr.depo.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Envanterle gelen aparat (şarj adaptörü, kablo…). Sınırlar ENVANTER_APARATI tablosuyla aynıdır.
 *
 * @param adet en az 1 (domain ve CK_ENVANTER_APARATI_ADET)
 */
public record CreateEnvanterAparatiRequest(
    @NotBlank(message = "{dogrulama.bos-olamaz}")
    @Size(max = 100, message = "{dogrulama.en-fazla}")
    String ad,

    @NotNull(message = "{dogrulama.zorunlu}")
    @Positive(message = "{dogrulama.pozitif}")
    Integer adet,

    @Size(max = 50, message = "{dogrulama.en-fazla}")
    String seriNo,

    @Size(max = 500, message = "{dogrulama.en-fazla}")
    String aciklama
) {
}
