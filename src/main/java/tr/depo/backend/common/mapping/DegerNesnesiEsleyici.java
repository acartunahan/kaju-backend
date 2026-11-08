package tr.depo.backend.common.mapping;

import org.springframework.stereotype.Component;
import tr.depo.domain.ortak.IletisimKisisi;
import tr.depo.domain.ortak.Sicil;

/**
 * Domain değer nesneleri ↔ API'deki düz alanlar. API'de değer nesneleri düz alan olarak taşınır (ör. sicil metin);
 * boş değer, değer nesnesinin yokluğu demektir.
 */
@Component
public class DegerNesnesiEsleyici {

    public String sicil(Sicil sicil) {
        return sicil == null ? null : sicil.deger();
    }

    /** Boş ya da yalnız boşluk → null; aksi halde trim edilmiş sicil (domain kuralı). */
    public Sicil sicil(String deger) {
        return Sicil.varsa(deger);
    }

    /** İki alan da boşsa iletişim kişisi yok sayılır. */
    public static IletisimKisisi iletisimKisisi(String ad, String telefon) {
        return ad == null && telefon == null ? null : new IletisimKisisi(ad, telefon);
    }
}
