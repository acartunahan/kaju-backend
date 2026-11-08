package tr.depo.backend.exception;

/**
 * İstek biçim olarak geçerli ama bir iş kuralını ihlal ediyor (422). Belirli bir kural için alt sınıf ya da
 * özel mesaj anahtarı kullanılır.
 */
public class IsKuraliException extends KajuException {

    public IsKuraliException(String mesajAnahtari, String logMesaji, Object... argumanlar) {
        super(HataKodu.IS_KURALI_IHLALI, mesajAnahtari, logMesaji, argumanlar);
    }

    protected IsKuraliException(HataKodu hataKodu, String mesajAnahtari, String logMesaji, Object... argumanlar) {
        super(hataKodu, mesajAnahtari, logMesaji, argumanlar);
    }
}
