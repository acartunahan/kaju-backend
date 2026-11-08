package tr.depo.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import tr.depo.domain.ariza.ArizaIslemTuru;

/**
 * Arıza işlemi tam güncellemesi (PUT).
 *
 * @param versiyon istemcinin okuduğu sürüm; güncel değilse 409
 */
public record UpdateArizaIslemiRequest(
    @NotNull(message = "{dogrulama.zorunlu}")
    @PositiveOrZero(message = "{dogrulama.sifir-veya-pozitif}")
    Integer versiyon,

    @NotNull(message = "{dogrulama.zorunlu}")
    ArizaIslemTuru tur,

    @NotNull(message = "{dogrulama.zorunlu}")
    BigDecimal islemKodu,

    @NotBlank(message = "{dogrulama.bos-olamaz}")
    @Size(max = 36, message = "{dogrulama.en-fazla}")
    String islemYapanSicil,

    @NotBlank(message = "{dogrulama.bos-olamaz}")
    @Size(max = 36, message = "{dogrulama.en-fazla}")
    String gorevliSicil,

    @NotNull(message = "{dogrulama.zorunlu}")
    LocalDateTime zaman,

    @NotNull(message = "{dogrulama.zorunlu}")
    LocalDateTime miad,

    String aciklama,

    Long siraNo
) {
}
