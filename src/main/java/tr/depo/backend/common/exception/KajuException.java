package tr.depo.backend.common.exception;

import java.util.Arrays;
import java.util.Objects;

/**
 * Uygulamanın bilinçli fırlattığı tüm hataların kökü. HTTP durumu ve mesaj anahtarı {@link HataKodu}'ndan gelir;
 * {@code GlobalExceptionHandler} bunu tek noktada ProblemDetail'e çevirir. Alt sınıflar hata türünü (ve dolayısıyla
 * kodu) belirler, servisler HTTP bilmez.
 *
 * <p>Mesaj ({@code getMessage()}) sadece log içindir ve Türkçe kullanıcı mesajı değildir; kullanıcı mesajı
 * {@code messages.properties}'teki anahtardan, {@link #argumanlar()} ile üretilir.
 */
public abstract class KajuException extends RuntimeException {

    private final HataKodu hataKodu;
    private final String mesajAnahtari;
    private final transient Object[] argumanlar;

    /**
     * Tek kurucu (bilerek): mesaj argümanları da çoğunlukla String olduğundan ikinci bir aşırı yükleme,
     * argümanın mesaj anahtarı sanılmasına yol açar.
     *
     * @param mesajAnahtari genelde {@code hataKodu.mesajAnahtari()}; daha özel bir mesaj için başka bir anahtar
     */
    protected KajuException(HataKodu hataKodu, String mesajAnahtari, String logMesaji, Object... argumanlar) {
        super(logMesaji);
        this.hataKodu = Objects.requireNonNull(hataKodu, "hataKodu");
        this.mesajAnahtari = Objects.requireNonNull(mesajAnahtari, "mesajAnahtari");
        this.argumanlar = argumanlar == null ? new Object[0] : argumanlar.clone();
    }

    public HataKodu hataKodu() {
        return hataKodu;
    }

    public String mesajAnahtari() {
        return mesajAnahtari;
    }

    public Object[] argumanlar() {
        return argumanlar.clone();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + hataKodu + ", " + getMessage() + ", " + Arrays.toString(argumanlar) + "]";
    }
}
