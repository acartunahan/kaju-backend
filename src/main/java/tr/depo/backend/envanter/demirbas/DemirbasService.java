package tr.depo.backend.envanter.demirbas;

import org.springframework.data.domain.Pageable;
import tr.depo.backend.common.paging.SayfaCevabi;
import tr.depo.backend.envanter.demirbas.dto.CreateDemirbasRequest;
import tr.depo.backend.envanter.demirbas.dto.DemirbasResponse;
import tr.depo.backend.envanter.demirbas.dto.UpdateDemirbasRequest;

public interface DemirbasService {

    /** @throws tr.depo.backend.common.exception.KayitBulunamadiException yoksa, silinmişse ya da demirbaş değilse */
    DemirbasResponse getir(Long id);

    /** Silinmemiş demirbaşlar, sayfalı. */
    SayfaCevabi<DemirbasResponse> listele(Pageable sayfa);

    DemirbasResponse olustur(CreateDemirbasRequest istek);

    DemirbasResponse guncelle(Long id, UpdateDemirbasRequest istek);

    /**
     * Soft delete; barkod, not, aparat ve fotoğraflar da aynı transaction'da silinir. Hareket, arıza ve sayım satırları
     * geçmiş kaydıdır, silinmez.
     */
    void sil(Long id);
}
