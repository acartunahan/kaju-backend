package tr.depo.backend.common.exception;

/** İstek parametreleri geçersiz (400); ör. izin verilmeyen bir alana göre sıralama. */
public class GecersizIstekException extends KajuException {

    public GecersizIstekException(HataKodu hataKodu, String logMesaji, Object... argumanlar) {
        super(hataKodu, hataKodu.mesajAnahtari(), logMesaji, argumanlar);
    }
}
