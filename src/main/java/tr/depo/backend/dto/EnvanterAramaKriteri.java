package tr.depo.backend.dto;

import java.util.Objects;
import tr.depo.domain.envanter.DemirbasDurumu;
import tr.depo.domain.ortak.Sicil;
import tr.depo.domain.tanim.MalzemeTuru;

/**
 * Demirbaş ve sarf aramalarının ortak kriteri (servisler arası; API'de doğrudan alınmaz). Boş alanlar uygulanmaz.
 *
 * @param q       "içerir" araması: ad, seri no, açıklama, barkod; sayısal ise demirbaş no da
 * @param barkod  tam eşleşme (barkod okutma)
 * @param seriNo  tam eşleşme
 */
public record EnvanterAramaKriteri(
    MalzemeTuru tur,
    Long kategoriId,
    boolean altKategoriler,
    Long markaId,
    Long modelId,
    Long depoId,
    Long ihaleId,
    DemirbasDurumu durum,
    Sicil zimmetliSicil,
    Boolean zimmetli,
    String q,
    String barkod,
    String seriNo,
    Long demirbasNo
) {

    public EnvanterAramaKriteri {
        Objects.requireNonNull(tur, "tur");
    }

    public static EnvanterAramaKriteri demirbas(DemirbasFiltreRequest f, Sicil zimmetliSicil) {
        return new EnvanterAramaKriteri(MalzemeTuru.DEMIRBAS, f.kategoriId(), !Boolean.FALSE.equals(f.altKategoriler()),
            f.markaId(), f.modelId(), f.depoId(), f.ihaleId(), f.durum(), zimmetliSicil, f.zimmetli(), bosIse(f.q()),
            null, null, null);
    }

    public static EnvanterAramaKriteri sarf(SarfMalzemesiFiltreRequest f) {
        return new EnvanterAramaKriteri(MalzemeTuru.SARF, f.kategoriId(), !Boolean.FALSE.equals(f.altKategoriler()),
            f.markaId(), f.modelId(), f.depoId(), null, null, null, null, bosIse(f.q()), null, null, null);
    }

    /** Barkod, seri no ya da demirbaş no ile tam eşleşme (yalnızca biri). */
    public static EnvanterAramaKriteri demirbasBul(String barkod, String seriNo, Long demirbasNo) {
        return new EnvanterAramaKriteri(MalzemeTuru.DEMIRBAS, null, false, null, null, null, null, null, null, null,
            null, bosIse(barkod), bosIse(seriNo), demirbasNo);
    }

    private static String bosIse(String deger) {
        return deger == null || deger.isBlank() ? null : deger.strip();
    }
}
