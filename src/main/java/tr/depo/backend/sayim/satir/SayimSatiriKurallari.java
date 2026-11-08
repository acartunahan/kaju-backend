package tr.depo.backend.sayim.satir;

import java.time.LocalDateTime;
import tr.depo.backend.common.exception.IsKuraliException;
import tr.depo.domain.sayim.SayimIslemi;
import tr.depo.domain.sayim.SayimKapsami;
import tr.depo.domain.sayim.YerelSayimTuru;

/**
 * Sayım satırının türe göre domain kuralları (SayimSatiri entity'si ve CK_SAYIM_SATIRI_* kısıtları). Entity bu
 * kuralları istisnayla zorlar; burada önceden kontrol edilerek anlamlı 422 döner.
 */
final class SayimSatiriKurallari {

    static final String ISLEM_TURE_UYGUN_DEGIL = "sayim-satiri.islem-tur";
    static final String YEREL_ZORUNLU = "sayim-satiri.yerel-zorunlu";
    static final String GENEL_TARIH_ZORUNLU = "sayim-satiri.genel-tarih-zorunlu";
    static final String ENVANTER_YALNIZ_ZIMMET = "sayim-satiri.envanter-yalniz-zimmet";
    static final String KAPSAM_UYUMSUZ = "sayim-satiri.kapsam-uyumsuz";

    private SayimSatiriKurallari() {
    }

    /**
     * Yeni satırın türü sayımın kapsamıyla aynı olmalı (yerel sayıma yerel satır). Yalnızca yeni satırda uygulanır:
     * domain bu tutarlılığı legacy veri yüzünden zorlamaz (HANDOFF 5.16) ve satırın türü sonradan değişmez.
     */
    static void kapsamaUygunOlmali(SayimKapsami satirTuru, SayimKapsami sayimKapsami) {
        if (satirTuru != sayimKapsami) {
            throw new IsKuraliException(KAPSAM_UYUMSUZ, satirTuru + " satırı " + sayimKapsami + " sayımına eklenemez",
                satirTuru, sayimKapsami);
        }
    }

    static void dogrula(SayimKapsami tur, SayimIslemi islem, LocalDateTime islemTarihi, YerelSayimTuru malzemeTuru,
                        String malzemeEskiRef, String zimmetEskiRef, Long envanterId) {
        if (!islem.gecerliMi(tur)) {
            throw new IsKuraliException(ISLEM_TURE_UYGUN_DEGIL, islem + " işlemi " + tur + " satırında geçersiz", islem, tur);
        }
        if (tur == SayimKapsami.YEREL) {
            yerelKurallari(malzemeTuru, malzemeEskiRef, zimmetEskiRef, envanterId);
        } else if (islemTarihi == null) {
            throw new IsKuraliException(GENEL_TARIH_ZORUNLU, "Genel sayım satırında islemTarihi zorunlu");
        }
    }

    private static void yerelKurallari(YerelSayimTuru malzemeTuru, String malzemeEskiRef, String zimmetEskiRef,
                                       Long envanterId) {
        if (malzemeTuru == null || bos(malzemeEskiRef) || bos(zimmetEskiRef)) {
            throw new IsKuraliException(YEREL_ZORUNLU, "Yerel sayım satırında malzemeTuru, malzemeEskiRef, zimmetEskiRef zorunlu");
        }
        if (envanterId != null && malzemeTuru != YerelSayimTuru.ZIMMET) {
            throw new IsKuraliException(ENVANTER_YALNIZ_ZIMMET, "Yerel sayımda envantere yalnızca ZIMMET satırı bağlanır");
        }
    }

    private static boolean bos(String deger) {
        return deger == null || deger.isBlank();
    }
}
