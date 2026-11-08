package tr.depo.backend.firma.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import tr.depo.domain.tanim.MalzemeTuru;

/**
 * Yeni firma. Sınırlar FIRMA tablosuyla aynıdır. Türe göre zorunluluk (domain): DEMIRBAS'ta {@code durumKodu} ve
 * {@code kayitTarihi}, SARF'ta {@code ad}. Yetkili ve irtibat kişisi iki alanı da boşsa yok sayılır.
 */
public record CreateFirmaRequest(
    @NotNull(message = "{dogrulama.zorunlu}")
    MalzemeTuru tur,

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
