package tr.depo.backend.envanter.barkod;

import org.springframework.data.domain.Pageable;
import tr.depo.backend.common.paging.SayfaCevabi;
import tr.depo.backend.envanter.barkod.dto.BarkodResponse;
import tr.depo.backend.envanter.barkod.dto.CreateBarkodRequest;
import tr.depo.backend.envanter.barkod.dto.UpdateBarkodRequest;

/** Envanterin barkodları; envanter yoksa, silinmişse ya da barkod başka envanterinse 404. */
public interface BarkodService {

    BarkodResponse getir(Long envanterId, Long id);

    SayfaCevabi<BarkodResponse> listele(Long envanterId, Pageable sayfa);

    BarkodResponse olustur(Long envanterId, CreateBarkodRequest istek);

    BarkodResponse guncelle(Long envanterId, Long id, UpdateBarkodRequest istek);

    void sil(Long envanterId, Long id);
}
