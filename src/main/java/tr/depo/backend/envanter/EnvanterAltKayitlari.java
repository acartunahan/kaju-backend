package tr.depo.backend.envanter;

import java.util.List;
import org.springframework.stereotype.Component;
import tr.depo.backend.common.service.SilmeAltyapisi;
import tr.depo.domain.envanter.Barkod;
import tr.depo.domain.envanter.Envanter;
import tr.depo.domain.envanter.EnvanterAparati;
import tr.depo.domain.envanter.EnvanterFotografi;
import tr.depo.domain.envanter.EnvanterNotu;
import tr.depo.domain.ortak.BaseEntity;

/**
 * Envanter aggregate'inin alt kayıtları: envanter silinince bunlar da aynı transaction'da soft delete edilir.
 * Hareket, arıza ve sayım satırları ayrı aggregate'lerdir (geçmiş kaydı); envanterle birlikte silinmez.
 * Fotoğrafın API'si henüz yok ama aggregate'e aittir; tabloda kaydı varsa o da silinir.
 */
@Component
public class EnvanterAltKayitlari {

    private static final String UST_ALANI = "envanter";
    private static final List<Class<? extends BaseEntity>> ALT_KAYIT_TURLERI =
        List.of(Barkod.class, EnvanterNotu.class, EnvanterAparati.class, EnvanterFotografi.class);

    private final SilmeAltyapisi silmeAltyapisi;

    public EnvanterAltKayitlari(SilmeAltyapisi silmeAltyapisi) {
        this.silmeAltyapisi = silmeAltyapisi;
    }

    public void sil(Envanter envanter, String silen) {
        ALT_KAYIT_TURLERI.forEach(tur -> silmeAltyapisi.aktifAltKayitlariSil(tur, UST_ALANI, envanter, silen));
    }
}
