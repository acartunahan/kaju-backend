package tr.depo.backend.common.exception;

/**
 * İstekte ID ile verilen ilişkili kayıt yok, silinmiş ya da türü uygun değil (422). {@code alan}, istekteki alanın
 * adıdır (ör. {@code depoId}); istemci hatayı ilgili form alanına bağlayabilsin diye cevapta döner.
 */
public class ReferansGecersizException extends IsKuraliException {

    private final String alan;

    public ReferansGecersizException(String alan, Object id) {
        super(HataKodu.REFERANS_GECERSIZ, HataKodu.REFERANS_GECERSIZ.mesajAnahtari(),
            "Referans verilen kayıt yok ya da silinmiş: " + alan + "=" + id, alan, id);
        this.alan = alan;
    }

    /** Kayıt var ama bu alana bağlanamıyor (ör. demirbaş kaydına sarf türündeki marka). */
    public static ReferansGecersizException turUyumsuz(String alan, Object id, Object beklenenTur) {
        return new ReferansGecersizException(alan, id, beklenenTur);
    }

    private ReferansGecersizException(String alan, Object id, Object beklenenTur) {
        super(HataKodu.REFERANS_GECERSIZ, "hata.referans-gecersiz.tur",
            "Referans verilen kaydın türü uygun değil: " + alan + "=" + id + ", beklenen=" + beklenenTur,
            alan, id, beklenenTur);
        this.alan = alan;
    }

    public String alan() {
        return alan;
    }
}
