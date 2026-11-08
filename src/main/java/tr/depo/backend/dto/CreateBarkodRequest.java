package tr.depo.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Envantere yeni barkod. Bir envanterin birden fazla barkodu olabilir; barkod değeri tekil değildir (legacy veride
 * tekrar olabilir).
 *
 * @param seriNoMu barkodun aynı zamanda seri numarası olup olmadığı
 */
public record CreateBarkodRequest(
    @NotBlank(message = "{dogrulama.bos-olamaz}")
    @Size(max = 50, message = "{dogrulama.en-fazla}")
    String barkod,

    @NotNull(message = "{dogrulama.zorunlu}")
    Boolean seriNoMu
) {
}
