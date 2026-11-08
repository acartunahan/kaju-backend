package tr.depo.backend.sayim.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import tr.depo.domain.sayim.SayimDurumu;
import tr.depo.domain.sayim.SayimKapsami;

/** Yeni sayım (başlık). Satırlar {@code /sayimlar/{sayimId}/satirlar} altında eklenir. */
public record CreateSayimRequest(
    @NotNull(message = "{dogrulama.zorunlu}")
    SayimKapsami kapsam,

    @NotBlank(message = "{dogrulama.bos-olamaz}")
    @Size(max = 60, message = "{dogrulama.en-fazla}")
    String baslik,

    LocalDateTime baslangicTarihi,

    LocalDateTime bitisTarihi,

    SayimDurumu durum,

    Long depoId
) {
}
