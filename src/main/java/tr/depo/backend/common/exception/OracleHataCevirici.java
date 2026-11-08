package tr.depo.backend.common.exception;

import java.sql.SQLException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.stereotype.Component;

/**
 * Oracle kısıt ihlallerini iş hatalarına çevirir. Kısıt adı hata mesajından okunur
 * ({@code ORA-00001: unique constraint (HEDEF.UK_X) violated}); kısıt adları domain şemasında anlamlıdır
 * ({@code UK_}, {@code CK_}, {@code FK_}), bu yüzden {@code kisit.<KISIT_ADI>} anahtarıyla özel mesaj verilebilir.
 *
 * <p>Bilerek çevrilmeyenler (500, tam log): ORA-02292 (alt kayıt var) ve ORA-20001 (fiziksel silme yasağı
 * trigger'ı). Uygulama hiçbir zaman fiziksel silme yapmadığı için bunlar ancak bir programlama hatasıyla oluşur.
 */
@Component
public class OracleHataCevirici {

    static final int TEKIL_KISIT = 1;
    static final int CHECK_KISIT = 2290;
    static final int UST_KAYIT_YOK = 2291;

    private static final String KISIT_ANAHTAR_ONEKI = "kisit.";
    private static final String BILINMEYEN_KISIT = "BILINMIYOR";
    private static final Pattern KISIT_ADI = Pattern.compile("\\((?:[A-Za-z0-9_$#\"]+\\.)?\"?([A-Za-z0-9_$#]+)\"?\\)");

    public Optional<KajuException> cevir(Throwable hata) {
        SQLException sqlHatasi = sqlHatasiBul(hata);
        if (sqlHatasi == null) {
            return Optional.empty();
        }
        String kisit = kisitAdi(sqlHatasi.getMessage());
        return switch (sqlHatasi.getErrorCode()) {
            case TEKIL_KISIT -> Optional.of(new TekillikIhlaliException(kisitMesajAnahtari(kisit), kisit));
            case CHECK_KISIT, UST_KAYIT_YOK -> Optional.of(new VeriKuraliException(kisit));
            default -> Optional.empty();
        };
    }

    static String kisitMesajAnahtari(String kisit) {
        return KISIT_ANAHTAR_ONEKI + kisit;
    }

    static String kisitAdi(String mesaj) {
        if (mesaj == null) {
            return BILINMEYEN_KISIT;
        }
        Matcher eslesme = KISIT_ADI.matcher(mesaj);
        return eslesme.find() ? eslesme.group(1) : BILINMEYEN_KISIT;
    }

    private static SQLException sqlHatasiBul(Throwable hata) {
        for (Throwable neden = hata; neden != null; neden = neden.getCause()) {
            if (neden instanceof SQLException sql && sql.getErrorCode() != 0) {
                return sql;
            }
        }
        return NestedExceptionUtils.getMostSpecificCause(hata) instanceof SQLException sql ? sql : null;
    }
}
