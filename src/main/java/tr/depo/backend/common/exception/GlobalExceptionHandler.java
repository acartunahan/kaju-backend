package tr.depo.backend.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Merkezi hata yönetimi: tüm hatalar RFC 9457 {@link ProblemDetail} olarak döner.
 *
 * <p>Cevap alanları: {@code type} ({@code urn:kaju:hata:<KOD>}), {@code title}, {@code status}, {@code detail},
 * {@code instance} (istek yolu) ve ek olarak {@code kod} ({@link HataKodu}); doğrulama hatalarında {@code hatalar}
 * (alan bazlı liste), referans hatalarında {@code alan}. Mesajlar {@code messages.properties}'ten Türkçe gelir.
 * Stack trace cevaba asla girmez; 5xx hatalar tam olarak loglanır.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    static final String KOD = "kod";
    static final String HATALAR = "hatalar";
    static final String ALAN = "alan";
    private static final String TIP_ONEKI = "urn:kaju:hata:";

    private final MessageSource mesajlar;
    private final OracleHataCevirici oracleHataCevirici;

    public GlobalExceptionHandler(MessageSource mesajlar, OracleHataCevirici oracleHataCevirici) {
        this.mesajlar = mesajlar;
        this.oracleHataCevirici = oracleHataCevirici;
    }

    @ExceptionHandler(KajuException.class)
    public ResponseEntity<ProblemDetail> kajuHatasi(KajuException hata, HttpServletRequest istek) {
        logla(hata.hataKodu(), hata);
        ProblemDetail problem = problem(hata.hataKodu(), hata.mesajAnahtari(), hata.argumanlar(), istek);
        if (hata instanceof ReferansGecersizException referans) {
            problem.setProperty(ALAN, referans.alan());
        }
        return cevap(problem);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ProblemDetail> surumCakismasi(OptimisticLockingFailureException hata, HttpServletRequest istek) {
        Object[] argumanlar = hata instanceof ObjectOptimisticLockingFailureException nesne
            ? new Object[] {kisaAd(nesne.getPersistentClassName()), nesne.getIdentifier()}
            : new Object[] {"Kayıt", "?"};
        logla(HataKodu.SURUM_CAKISMASI, hata);
        return cevap(problem(HataKodu.SURUM_CAKISMASI, HataKodu.SURUM_CAKISMASI.mesajAnahtari(), argumanlar, istek));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> veriButunlugu(DataIntegrityViolationException hata, HttpServletRequest istek) {
        return oracleHataCevirici.cevir(hata)
            .map(cevrilen -> {
                log.info("Veritabanı kısıtı iş hatasına çevrildi: {}", cevrilen.getMessage());
                return kajuHatasi(cevrilen, istek);
            })
            .orElseGet(() -> beklenmeyen(hata, istek));
    }

    /** Sıralama doğrulamasından kaçan geçersiz bir özellik adı (savunma hattı). */
    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ProblemDetail> gecersizOzellik(PropertyReferenceException hata, HttpServletRequest istek) {
        logla(HataKodu.GECERSIZ_SIRALAMA, hata);
        return cevap(problem(HataKodu.GECERSIZ_SIRALAMA, HataKodu.GECERSIZ_SIRALAMA.mesajAnahtari(),
            new Object[] {hata.getPropertyName()}, istek));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> beklenmeyen(Exception hata, HttpServletRequest istek) {
        log.error("Beklenmeyen hata: {} {}", istek.getMethod(), istek.getRequestURI(), hata);
        return cevap(problem(HataKodu.BEKLENMEYEN_HATA, HataKodu.BEKLENMEYEN_HATA.mesajAnahtari(), new Object[0], istek));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException hata,
                                                                  HttpHeaders basliklar, HttpStatusCode durum,
                                                                  WebRequest istek) {
        List<AlanHatasi> hatalar = hata.getBindingResult().getAllErrors().stream().map(this::alanHatasi).toList();
        ProblemDetail problem = problem(HataKodu.DOGRULAMA_HATASI, HataKodu.DOGRULAMA_HATASI.mesajAnahtari(),
            new Object[] {hatalar.size()}, null);
        problem.setProperty(HATALAR, hatalar);
        log.debug("Doğrulama hatası: {}", hatalar);
        return handleExceptionInternal(hata, problem, basliklar, HttpStatus.BAD_REQUEST, istek);
    }

    /**
     * Spring'in kendi hataları (bozuk JSON, desteklenmeyen metot, bilinmeyen yol…) için de aynı biçim:
     * {@code kod} eklenir, başlık ve açıklama Türkçeleştirilir.
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception hata, Object govde, HttpHeaders basliklar,
                                                             HttpStatusCode durum, WebRequest istek) {
        // Spring çoğu kendi hatasında gövdeyi null geçer ve ProblemDetail'i hatanın kendisinden (ErrorResponse) alır.
        if (govde == null && hata instanceof ErrorResponse hataCevabi) {
            govde = hataCevabi.getBody();
        }
        if (govde instanceof ProblemDetail problem) {
            if (!kodVar(problem)) {
                HataKodu kod = durumaGoreKod(durum);
                problem.setType(URI.create(TIP_ONEKI + kod.name()));
                problem.setTitle(mesaj(kod.baslikAnahtari(), new Object[0]));
                problem.setDetail(mesaj(kod.mesajAnahtari(), new Object[0]));
                problem.setProperty(KOD, kod.name());
                logla(kod, hata);
            }
            if (problem.getInstance() == null && istek instanceof ServletWebRequest servlet) {
                problem.setInstance(URI.create(servlet.getRequest().getRequestURI()));
            }
        }
        return super.handleExceptionInternal(hata, govde, basliklar, durum, istek);
    }

    private static boolean kodVar(ProblemDetail problem) {
        return problem.getProperties() != null && problem.getProperties().containsKey(KOD);
    }

    private ProblemDetail problem(HataKodu kod, String mesajAnahtari, Object[] argumanlar, HttpServletRequest istek) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(kod.durum(),
            mesaj(mesajAnahtari, argumanlar, kod.mesajAnahtari()));
        problem.setType(URI.create(TIP_ONEKI + kod.name()));
        problem.setTitle(mesaj(kod.baslikAnahtari(), new Object[0]));
        problem.setProperty(KOD, kod.name());
        if (istek != null) {
            problem.setInstance(URI.create(istek.getRequestURI()));
        }
        return problem;
    }

    private AlanHatasi alanHatasi(ObjectError hata) {
        String alan = hata instanceof FieldError alanHatasi ? alanHatasi.getField() : hata.getObjectName();
        return new AlanHatasi(alan, mesajlar.getMessage(hata, yerel()));
    }

    /** Özel anahtar yoksa (ör. tanımlanmamış bir {@code kisit.X}) hata kodunun genel mesajına düşer. */
    private String mesaj(String anahtar, Object[] argumanlar, String yedekAnahtar) {
        String yedek = anahtar.equals(yedekAnahtar) ? anahtar : mesajlar.getMessage(yedekAnahtar, argumanlar, yedekAnahtar, yerel());
        return mesajlar.getMessage(anahtar, argumanlar, yedek, yerel());
    }

    private String mesaj(String anahtar, Object[] argumanlar) {
        return mesajlar.getMessage(anahtar, argumanlar, anahtar, yerel());
    }

    private static Locale yerel() {
        return LocaleContextHolder.getLocale();
    }

    private static HataKodu durumaGoreKod(HttpStatusCode durum) {
        return switch (durum.value()) {
            case 400 -> HataKodu.GECERSIZ_ISTEK;
            case 404 -> HataKodu.KAYNAK_BULUNAMADI;
            case 405 -> HataKodu.METOT_DESTEKLENMIYOR;
            default -> durum.is5xxServerError() ? HataKodu.BEKLENMEYEN_HATA : HataKodu.GECERSIZ_ISTEK;
        };
    }

    private static void logla(HataKodu kod, Exception hata) {
        if (kod.durum().is5xxServerError()) {
            log.error("{}: {}", kod, hata.getMessage(), hata);
        } else {
            log.info("{}: {}", kod, hata.getMessage());
        }
    }

    private static ResponseEntity<ProblemDetail> cevap(ProblemDetail problem) {
        return ResponseEntity.status(problem.getStatus()).body(problem);
    }

    private static String kisaAd(String sinifAdi) {
        return sinifAdi == null ? "Kayıt" : sinifAdi.substring(sinifAdi.lastIndexOf('.') + 1);
    }
}
