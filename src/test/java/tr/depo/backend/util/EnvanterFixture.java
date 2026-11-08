package tr.depo.backend.util;

import java.util.concurrent.atomic.AtomicLong;

/** Envanter kaynakları (demirbaş, sarf, barkod, not, aparat) için geçerli varsayılan istek gövdeleri. */
public final class EnvanterFixture {

    /** Demirbaş no ve seri no tekil olmalı: her çağrıda yeni değer (testler aynı veritabanını paylaşır). */
    private static final AtomicLong SAYAC = new AtomicLong(100_000);

    private EnvanterFixture() {
    }

    public static IstekGovdesi demirbas() {
        long numara = SAYAC.incrementAndGet();
        return IstekGovdesi.yeni("durum", "NORMAL", "kayitTarihi", TanimFixture.TARIH, "demirbasNo", numara,
            "seriNo", "SN-" + numara, "ad", "Dizüstü Bilgisayar", "alimYolu", "BILGI_ISLEM_DB", "zimmetliSicil", "01234",
            "garantiBaslangic", "2026-01-01T00:00:00", "garantiBitis", "2028-01-01T00:00:00");
    }

    public static IstekGovdesi sarf(String ad) {
        return IstekGovdesi.yeni("ad", ad, "aciklama", "Ofis sarfı", "durumKodu", "1", "kayitTarihi", TanimFixture.TARIH);
    }

    public static IstekGovdesi barkod(String barkod) {
        return IstekGovdesi.yeni("barkod", barkod, "seriNoMu", false);
    }

    public static IstekGovdesi not(String metin) {
        return IstekGovdesi.yeni("notMetni", metin, "ekleyenAdi", "Ayşe Yılmaz", "ekleyenSicil", "0012345");
    }

    public static IstekGovdesi aparat(String ad, int adet) {
        return IstekGovdesi.yeni("ad", ad, "adet", adet, "seriNo", "AP-1");
    }
}
