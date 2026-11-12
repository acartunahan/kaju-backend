package tr.depo.backend.service;

import org.springframework.data.domain.Pageable;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.ZimmetGecmisiResponse;
import tr.depo.domain.envanter.Envanter;
import tr.depo.domain.ortak.Sicil;

/**
 * Demirbaşların zimmet geçmişi. Kayıtlar yalnızca demirbaşın zimmetli sicili değiştiğinde yazılır (değiştirilemez,
 * silinmez); API'de yalnızca okunur.
 */
public interface ZimmetGecmisiService {

    /**
     * Demirbaşın zimmetli sicili {@code oncekiSicil}'den güncel değerine değiştiyse geçmişe yazar; değişmediyse bir
     * şey yapmaz. Demirbaşı kaydeden transaction içinde, demirbaş kaydedildikten sonra çağrılır.
     */
    void degisiklikVarsaKaydet(Envanter demirbas, Sicil oncekiSicil);

    /**
     * Demirbaşın zimmet geçmişi, sayfalı. Sıralama verilmezse en yeni değişiklik önce.
     *
     * @throws tr.depo.backend.exception.KayitBulunamadiException demirbaş yoksa, silinmişse ya da sarf ise (404)
     */
    SayfaResponse<ZimmetGecmisiResponse> listele(Long demirbasId, Pageable sayfa);
}
