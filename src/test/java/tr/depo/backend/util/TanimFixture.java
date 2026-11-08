package tr.depo.backend.util;

/** Tanım kaynakları (depo, firma, kategori, model, ihale) için geçerli varsayılan istek gövdeleri. */
public final class TanimFixture {

    public static final String TARIH = "2026-01-15T10:30:00";

    private TanimFixture() {
    }

    public static IstekGovdesi depo() {
        return IstekGovdesi.yeni("ad", "Merkez Depo", "aciklama", "Ana depo", "durumKodu", "1",
            "kayitTarihi", TARIH, "sorumluSicil", "0001234");
    }

    public static IstekGovdesi demirbasFirmasi() {
        return IstekGovdesi.yeni("tur", "DEMIRBAS", "ad", "Bilişim A.Ş.", "durumKodu", "1", "kayitTarihi", TARIH,
            "yetkiliAd", "Ayşe Yılmaz", "yetkiliTelefon", "0312 000 00 00", "telefon1", "0312 111 11 11");
    }

    public static IstekGovdesi sarfFirmasi() {
        return IstekGovdesi.yeni("tur", "SARF", "ad", "Kırtasiye Ltd.");
    }

    public static IstekGovdesi demirbasKategorisi(String ad) {
        return IstekGovdesi.yeni("tur", "DEMIRBAS", "ad", ad, "durumKodu", "1", "kayitTarihi", TARIH);
    }

    public static IstekGovdesi sarfKategorisi(String ad) {
        return IstekGovdesi.yeni("tur", "SARF", "ad", ad);
    }

    /** Marka her iki türde zorunlu. */
    public static IstekGovdesi demirbasModeli(String ad, long markaId) {
        return IstekGovdesi.yeni("tur", "DEMIRBAS", "ad", ad, "markaId", markaId, "durumKodu", "1", "kayitTarihi", TARIH);
    }

    public static IstekGovdesi sarfModeli(String ad, long markaId) {
        return IstekGovdesi.yeni("tur", "SARF", "ad", ad, "markaId", markaId, "aktif", true);
    }

    public static IstekGovdesi ihale() {
        return IstekGovdesi.yeni("ad", "2026/15 Bilgisayar Alımı", "ihaleTarihi", TARIH, "durumKodu", "1",
            "kayitTarihi", TARIH, "komisyonBaskani", "Başkan", "komisyonUye1", "Üye 1", "birimKodu", 3);
    }
}
