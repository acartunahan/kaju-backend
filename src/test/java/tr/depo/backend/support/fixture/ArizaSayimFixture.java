package tr.depo.backend.support.fixture;

/** Arıza ve sayım kaynakları için geçerli varsayılan istek gövdeleri. */
public final class ArizaSayimFixture {

    private ArizaSayimFixture() {
    }

    public static IstekGovdesi ariza() {
        return IstekGovdesi.yeni("bildirenSicil", "0012345", "sebep", "Ekran açılmıyor", "asamaKodu", 1,
            "tarih", TanimFixture.TARIH, "miad", "2026-01-20T17:00:00", "iletisimTelefonu", "0312 000 00 00");
    }

    public static IstekGovdesi arizaIslemi() {
        return IstekGovdesi.yeni("tur", "ISLEM", "islemKodu", 2, "islemYapanSicil", "0012345",
            "gorevliSicil", "0054321", "zaman", TanimFixture.TARIH, "miad", "2026-01-20T17:00:00",
            "aciklama", "Teknik servise gönderildi", "siraNo", 1);
    }

    public static IstekGovdesi sayim(String kapsam) {
        return IstekGovdesi.yeni("kapsam", kapsam, "baslik", "2026 Yıl Sonu Sayımı", "baslangicTarihi", TanimFixture.TARIH,
            "durum", "DEVAM_EDIYOR");
    }

    public static IstekGovdesi yerelSatir(String malzemeTuru) {
        return IstekGovdesi.yeni("tur", "YEREL", "islem", "SAYILDI", "malzemeTuru", malzemeTuru,
            "malzemeEskiRef", "{A1B2C3}", "zimmetEskiRef", "0012345", "islemTarihi", TanimFixture.TARIH);
    }

    public static IstekGovdesi genelSatir() {
        return IstekGovdesi.yeni("tur", "GENEL", "islem", "DUZELTME", "islemTarihi", TanimFixture.TARIH,
            "anlikAd", "Dizüstü Bilgisayar", "anlikMarka", "Dell", "anlikSeriNo", "SN-1", "merkezNotu", "Kontrol edildi");
    }
}
