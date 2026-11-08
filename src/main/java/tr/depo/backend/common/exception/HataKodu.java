package tr.depo.backend.common.exception;

import org.springframework.http.HttpStatus;

/**
 * API hata kodları: HTTP durumu ve {@code messages.properties} anahtarı tek yerde.
 * Cevaptaki {@code kod} alanı bu enum'un adıdır; istemciler mesaj metnine değil koda göre karar verir.
 */
public enum HataKodu {

    GECERSIZ_ISTEK(HttpStatus.BAD_REQUEST, "hata.gecersiz-istek"),
    DOGRULAMA_HATASI(HttpStatus.BAD_REQUEST, "hata.dogrulama"),
    GECERSIZ_SIRALAMA(HttpStatus.BAD_REQUEST, "hata.gecersiz-siralama"),
    KAYIT_BULUNAMADI(HttpStatus.NOT_FOUND, "hata.kayit-bulunamadi"),
    KAYNAK_BULUNAMADI(HttpStatus.NOT_FOUND, "hata.kaynak-bulunamadi"),
    METOT_DESTEKLENMIYOR(HttpStatus.METHOD_NOT_ALLOWED, "hata.metot-desteklenmiyor"),
    SURUM_CAKISMASI(HttpStatus.CONFLICT, "hata.surum-cakismasi"),
    TEKILLIK_IHLALI(HttpStatus.CONFLICT, "hata.tekillik-ihlali"),
    KAYIT_KULLANIMDA(HttpStatus.CONFLICT, "hata.kayit-kullanimda"),
    REFERANS_GECERSIZ(HttpStatus.UNPROCESSABLE_CONTENT, "hata.referans-gecersiz"),
    IS_KURALI_IHLALI(HttpStatus.UNPROCESSABLE_CONTENT, "hata.is-kurali"),
    VERI_KURALI_IHLALI(HttpStatus.UNPROCESSABLE_CONTENT, "hata.veri-kurali"),
    BEKLENMEYEN_HATA(HttpStatus.INTERNAL_SERVER_ERROR, "hata.beklenmeyen");

    private final HttpStatus durum;
    private final String mesajAnahtari;

    HataKodu(HttpStatus durum, String mesajAnahtari) {
        this.durum = durum;
        this.mesajAnahtari = mesajAnahtari;
    }

    public HttpStatus durum() {
        return durum;
    }

    public String mesajAnahtari() {
        return mesajAnahtari;
    }

    /** ProblemDetail başlığının anahtarı: {@code <mesajAnahtari>.baslik}. */
    public String baslikAnahtari() {
        return mesajAnahtari + ".baslik";
    }
}
