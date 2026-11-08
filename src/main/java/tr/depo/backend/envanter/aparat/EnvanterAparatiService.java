package tr.depo.backend.envanter.aparat;

import org.springframework.data.domain.Pageable;
import tr.depo.backend.common.paging.SayfaCevabi;
import tr.depo.backend.envanter.aparat.dto.CreateEnvanterAparatiRequest;
import tr.depo.backend.envanter.aparat.dto.EnvanterAparatiResponse;
import tr.depo.backend.envanter.aparat.dto.UpdateEnvanterAparatiRequest;

/** Envanterin aparatları; envanter yoksa, silinmişse ya da aparat başka envanterinse 404. */
public interface EnvanterAparatiService {

    EnvanterAparatiResponse getir(Long envanterId, Long id);

    SayfaCevabi<EnvanterAparatiResponse> listele(Long envanterId, Pageable sayfa);

    EnvanterAparatiResponse olustur(Long envanterId, CreateEnvanterAparatiRequest istek);

    EnvanterAparatiResponse guncelle(Long envanterId, Long id, UpdateEnvanterAparatiRequest istek);

    void sil(Long envanterId, Long id);
}
