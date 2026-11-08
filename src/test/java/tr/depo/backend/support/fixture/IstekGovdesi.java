package tr.depo.backend.support.fixture;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Web ve entegrasyon testleri için JSON istek gövdesi. Varsayılan geçerli değerlerle başlar; test yalnızca
 * kendisini ilgilendiren alanı {@link #ile} ile değiştirir ya da {@link #olmadan} ile çıkarır. Null alanlar yazılmaz.
 */
public final class IstekGovdesi {

    private final Map<String, Object> alanlar = new LinkedHashMap<>();

    private IstekGovdesi() {
    }

    public static IstekGovdesi yeni(Object... anahtarDegerler) {
        IstekGovdesi govde = new IstekGovdesi();
        for (int i = 0; i < anahtarDegerler.length; i += 2) {
            govde.alanlar.put((String) anahtarDegerler[i], anahtarDegerler[i + 1]);
        }
        return govde;
    }

    public IstekGovdesi ile(String alan, Object deger) {
        alanlar.put(alan, deger);
        return this;
    }

    public IstekGovdesi olmadan(String alan) {
        alanlar.remove(alan);
        return this;
    }

    /** PUT gövdesi için: aynı alanlar + {@code versiyon}. */
    public IstekGovdesi guncelleme(int versiyon) {
        IstekGovdesi kopya = new IstekGovdesi();
        kopya.alanlar.putAll(alanlar);
        kopya.alanlar.remove("tur");
        kopya.alanlar.put("versiyon", versiyon);
        return kopya;
    }

    public String json() {
        return yaz(alanlar);
    }

    static String yaz(Map<String, Object> alanlar) {
        return alanlar.entrySet().stream()
            .filter(alan -> alan.getValue() != null)
            .map(alan -> "\"" + alan.getKey() + "\":" + deger(alan.getValue()))
            .collect(Collectors.joining(",", "{", "}"));
    }

    private static String deger(Object deger) {
        return deger instanceof Number || deger instanceof Boolean ? deger.toString() : "\"" + deger + "\"";
    }
}
