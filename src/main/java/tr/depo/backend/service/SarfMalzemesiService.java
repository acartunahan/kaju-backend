package tr.depo.backend.service;

import org.springframework.data.domain.Pageable;
import tr.depo.backend.dto.CreateSarfMalzemesiRequest;
import tr.depo.backend.dto.SarfMalzemesiResponse;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.UpdateSarfMalzemesiRequest;

public interface SarfMalzemesiService {

    /** @throws tr.depo.backend.exception.KayitBulunamadiException yoksa, silinmişse ya da sarf değilse */
    SarfMalzemesiResponse getir(Long id);

    /** Silinmemiş sarf malzemeleri, sayfalı. */
    SayfaResponse<SarfMalzemesiResponse> listele(Pageable sayfa);

    SarfMalzemesiResponse olustur(CreateSarfMalzemesiRequest istek);

    SarfMalzemesiResponse guncelle(Long id, UpdateSarfMalzemesiRequest istek);

    /** Soft delete; alt kayıtları da silinir, stok hareketleri geçmiş kaydı olarak kalır. */
    void sil(Long id);
}
