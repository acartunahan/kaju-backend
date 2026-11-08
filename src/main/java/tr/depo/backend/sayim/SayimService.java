package tr.depo.backend.sayim;

import org.springframework.data.domain.Pageable;
import tr.depo.backend.common.paging.SayfaCevabi;
import tr.depo.backend.sayim.dto.CreateSayimRequest;
import tr.depo.backend.sayim.dto.SayimResponse;
import tr.depo.backend.sayim.dto.UpdateSayimRequest;

public interface SayimService {

    /** @throws tr.depo.backend.common.exception.KayitBulunamadiException yoksa ya da silinmişse */
    SayimResponse getir(Long id);

    /** Silinmemiş sayımlar, sayfalı. */
    SayfaCevabi<SayimResponse> listele(Pageable sayfa);

    SayimResponse olustur(CreateSayimRequest istek);

    SayimResponse guncelle(Long id, UpdateSayimRequest istek);

    /** Soft delete; sayım satırları da aynı transaction'da silinir. */
    void sil(Long id);
}
