package tr.depo.backend.common.exception;

/** İstek, kaydın mevcut durumuyla çakışıyor (409). Somut türler: sürüm, tekillik, kullanımda. */
public abstract class CakismaException extends KajuException {

    protected CakismaException(HataKodu hataKodu, String mesajAnahtari, String logMesaji, Object... argumanlar) {
        super(hataKodu, mesajAnahtari, logMesaji, argumanlar);
    }
}
