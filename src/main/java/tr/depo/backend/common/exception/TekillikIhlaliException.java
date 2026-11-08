package tr.depo.backend.common.exception;

/**
 * Tekil olması gereken bir değer zaten kullanılıyor (409). Kısıt adına özel mesaj varsa
 * ({@code kisit.<KISIT_ADI>}) o kullanılır.
 */
public class TekillikIhlaliException extends CakismaException {

    private final String kisit;

    public TekillikIhlaliException(String mesajAnahtari, String kisit) {
        super(HataKodu.TEKILLIK_IHLALI, mesajAnahtari, "Tekillik ihlali: " + kisit);
        this.kisit = kisit;
    }

    public String kisit() {
        return kisit;
    }
}
