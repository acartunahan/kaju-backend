package tr.depo.backend.envanter.not;

import org.springframework.data.domain.Pageable;
import tr.depo.backend.common.paging.SayfaCevabi;
import tr.depo.backend.envanter.not.dto.CreateEnvanterNotuRequest;
import tr.depo.backend.envanter.not.dto.EnvanterNotuResponse;
import tr.depo.backend.envanter.not.dto.UpdateEnvanterNotuRequest;

/** Envanterin notları; envanter yoksa, silinmişse ya da not başka envanterinse 404. */
public interface EnvanterNotuService {

    EnvanterNotuResponse getir(Long envanterId, Long id);

    SayfaCevabi<EnvanterNotuResponse> listele(Long envanterId, Pageable sayfa);

    EnvanterNotuResponse olustur(Long envanterId, CreateEnvanterNotuRequest istek);

    EnvanterNotuResponse guncelle(Long envanterId, Long id, UpdateEnvanterNotuRequest istek);

    void sil(Long envanterId, Long id);
}
