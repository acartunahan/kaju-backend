package tr.depo.backend.support.fixture;

/** Geçmiş kayıtları (envanter hareketi, zimmet kaydı) için geçerli varsayılan istek gövdeleri. */
public final class GecmisKayitFixture {

    private GecmisKayitFixture() {
    }

    public static IstekGovdesi demirbasHareketi(long envanterId) {
        return IstekGovdesi.yeni("tur", "DEMIRBAS", "envanterId", envanterId, "hareketNoKodu", 1, "hareketAdi", "Transfer",
            "nereyeMetni", "Ankara Şube", "islemYapanSicil", "123456", "kayitTarihi", TanimFixture.TARIH);
    }

    public static IstekGovdesi sarfHareketi(long envanterId) {
        return IstekGovdesi.yeni("tur", "SARF", "envanterId", envanterId, "miktar", 12.5, "yonKodu", "gelen",
            "alimTarihi", TanimFixture.TARIH, "kayitTarihi", TanimFixture.TARIH);
    }

    public static IstekGovdesi zimmetKaydi() {
        return IstekGovdesi.yeni("sicil", "0012345", "islem", "ZIMMET", "bilgi", "2 adet toner", "tarih", TanimFixture.TARIH,
            "islemYapanSicil", "0099999");
    }
}
