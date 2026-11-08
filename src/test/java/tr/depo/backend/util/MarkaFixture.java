package tr.depo.backend.util;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import tr.depo.backend.dto.CreateMarkaRequest;
import tr.depo.backend.dto.UpdateMarkaRequest;
import tr.depo.domain.tanim.MalzemeTuru;

/**
 * Marka test verisi. Varsayılanlar geçerli bir istektir; test yalnızca kendisini ilgilendiren alanı değiştirir.
 * {@link #json()} web testleri için istek gövdesi üretir (null alanlar yazılmaz).
 */
public final class MarkaFixture {

    private MalzemeTuru tur = MalzemeTuru.DEMIRBAS;
    private String ad = "Test Marka";
    private String aciklama = "Açıklama";
    private String durumKodu = "1";
    private LocalDateTime kayitTarihi = LocalDateTime.of(2026, 1, 15, 10, 30);
    private Boolean aktif;
    private Long depoId;
    private Integer versiyon = 0;

    private MarkaFixture() {
    }

    public static MarkaFixture demirbasMarkasi() {
        return new MarkaFixture();
    }

    public static MarkaFixture sarfMarkasi() {
        return new MarkaFixture().tur(MalzemeTuru.SARF).aktif(true);
    }

    public MarkaFixture tur(MalzemeTuru deger) {
        this.tur = deger;
        return this;
    }

    public MarkaFixture ad(String deger) {
        this.ad = deger;
        return this;
    }

    public MarkaFixture aciklama(String deger) {
        this.aciklama = deger;
        return this;
    }

    public MarkaFixture durumKodu(String deger) {
        this.durumKodu = deger;
        return this;
    }

    public MarkaFixture aktif(Boolean deger) {
        this.aktif = deger;
        return this;
    }

    public MarkaFixture depoId(Long deger) {
        this.depoId = deger;
        return this;
    }

    public MarkaFixture versiyon(Integer deger) {
        this.versiyon = deger;
        return this;
    }

    public CreateMarkaRequest olusturmaIstegi() {
        return new CreateMarkaRequest(tur, ad, aciklama, durumKodu, kayitTarihi, aktif, depoId);
    }

    public UpdateMarkaRequest guncellemeIstegi() {
        return new UpdateMarkaRequest(versiyon, ad, aciklama, durumKodu, kayitTarihi, aktif, depoId);
    }

    /** POST gövdesi. */
    public String json() {
        Map<String, Object> alanlar = ortakAlanlar();
        alanlar.put("tur", tur);
        return jsonYaz(alanlar);
    }

    /** PUT gövdesi. */
    public String guncellemeJson() {
        Map<String, Object> alanlar = ortakAlanlar();
        alanlar.put("versiyon", versiyon);
        return jsonYaz(alanlar);
    }

    private Map<String, Object> ortakAlanlar() {
        Map<String, Object> alanlar = new LinkedHashMap<>();
        alanlar.put("ad", ad);
        alanlar.put("aciklama", aciklama);
        alanlar.put("durumKodu", durumKodu);
        alanlar.put("kayitTarihi", kayitTarihi);
        alanlar.put("aktif", aktif);
        alanlar.put("depoId", depoId);
        return alanlar;
    }

    private static String jsonYaz(Map<String, Object> alanlar) {
        return IstekGovdesi.yaz(alanlar);
    }
}
