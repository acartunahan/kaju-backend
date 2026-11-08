package tr.depo.backend.common.exception;

/**
 * Silinmek istenen kayda başka aktif (silinmemiş) kayıtlar referans veriyor (409).
 * Argümanlar: silinen kaynak, id, referans veren kaynak, aktif referans sayısı.
 */
public class KayitKullanimdaException extends CakismaException {

    public KayitKullanimdaException(String kaynak, Object id, String referansVerenKaynak, long sayi) {
        super(HataKodu.KAYIT_KULLANIMDA, HataKodu.KAYIT_KULLANIMDA.mesajAnahtari(),
            kaynak + " kullanımda, silinemez: id=" + id + ", " + referansVerenKaynak + "=" + sayi,
            kaynak, id, referansVerenKaynak, sayi);
    }
}
