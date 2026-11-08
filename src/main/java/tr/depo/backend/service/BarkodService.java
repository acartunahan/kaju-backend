package tr.depo.backend.service;

import org.springframework.data.domain.Pageable;
import tr.depo.backend.dto.BarkodResponse;
import tr.depo.backend.dto.CreateBarkodRequest;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.UpdateBarkodRequest;

/** Envanterin barkodları; envanter yoksa, silinmişse ya da barkod başka envanterinse 404. */
public interface BarkodService {

    BarkodResponse getir(Long envanterId, Long id);

    SayfaResponse<BarkodResponse> listele(Long envanterId, Pageable sayfa);

    BarkodResponse olustur(Long envanterId, CreateBarkodRequest istek);

    BarkodResponse guncelle(Long envanterId, Long id, UpdateBarkodRequest istek);

    void sil(Long envanterId, Long id);
}
