package tr.depo.backend.util;

import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import tr.depo.backend.exception.GecersizIstekException;
import tr.depo.backend.exception.HataKodu;

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

    /** Sıralama verilmemişse varsayılan sıralamayla aynı sayfa (ör. geçmiş listelerinde en yeni önce). */
    public static Pageable varsayilanSiralama(Pageable sayfa, Sort varsayilan) {
        return sayfa.getSort().isSorted() || sayfa.isUnpaged()
            ? sayfa
            : PageRequest.of(sayfa.getPageNumber(), sayfa.getPageSize(), varsayilan);
    }
}
