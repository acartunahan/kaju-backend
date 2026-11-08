package tr.depo.backend.envanter.sarf;

import org.springframework.data.domain.Pageable;
import tr.depo.backend.common.paging.SayfaCevabi;
import tr.depo.backend.envanter.sarf.dto.CreateSarfMalzemesiRequest;
import tr.depo.backend.envanter.sarf.dto.SarfMalzemesiResponse;
import tr.depo.backend.envanter.sarf.dto.UpdateSarfMalzemesiRequest;

public interface SarfMalzemesiService {

    /** @throws tr.depo.backend.common.exception.KayitBulunamadiException yoksa, silinmişse ya da sarf değilse */
    SarfMalzemesiResponse getir(Long id);

    /** Silinmemiş sarf malzemeleri, sayfalı. */
    SayfaCevabi<SarfMalzemesiResponse> listele(Pageable sayfa);

    SarfMalzemesiResponse olustur(CreateSarfMalzemesiRequest istek);

    SarfMalzemesiResponse guncelle(Long id, UpdateSarfMalzemesiRequest istek);

    /** Soft delete; alt kayıtları da silinir, stok hareketleri geçmiş kaydı olarak kalır. */
    void sil(Long id);
}
