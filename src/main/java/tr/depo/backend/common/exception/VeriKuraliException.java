package tr.depo.backend.common.exception;

/**
 * Veritabanı bir veri kuralını (CHECK ya da yabancı anahtar) reddetti ve uygulama bunu daha önce yakalamadı (422).
 * Kısıt adına özel mesaj varsa ({@code kisit.<KISIT_ADI>}) o kullanılır, yoksa genel mesaj.
 */
public class VeriKuraliException extends IsKuraliException {

    private final String kisit;

    public VeriKuraliException(String kisit) {
        super(HataKodu.VERI_KURALI_IHLALI, OracleHataCevirici.kisitMesajAnahtari(kisit), "Veri kuralı ihlali: " + kisit);
        this.kisit = kisit;
    }

    public String kisit() {
        return kisit;
    }
}
