package tr.depo.backend.common.paging;

import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import tr.depo.backend.common.exception.GecersizIstekException;
import tr.depo.backend.common.exception.HataKodu;

/**
 * Sıralama alanlarını kaynak bazında izin listesine göre doğrular. Neden: istemcinin keyfi bir özellik yoluna
 * (ör. LAZY bir ilişkinin alanına) göre sıralama yapması hem hataya hem de istenmeyen join'lere yol açar.
 */
public final class SiralamaDogrulayici {

    private SiralamaDogrulayici() {
    }

    /**
     * @throws GecersizIstekException izin verilmeyen bir alana göre sıralama istenmişse (400)
     */
    public static Pageable dogrula(Pageable sayfa, Set<String> izinliAlanlar) {
        for (Sort.Order siralama : sayfa.getSort()) {
            if (!izinliAlanlar.contains(siralama.getProperty())) {
                throw new GecersizIstekException(HataKodu.GECERSIZ_SIRALAMA,
                    "İzin verilmeyen sıralama alanı: " + siralama.getProperty(), siralama.getProperty());
            }
        }
        return sayfa;
    }
}
