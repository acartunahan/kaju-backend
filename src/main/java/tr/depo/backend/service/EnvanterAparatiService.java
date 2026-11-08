package tr.depo.backend.service;

import org.springframework.data.domain.Pageable;
import tr.depo.backend.dto.CreateEnvanterAparatiRequest;
import tr.depo.backend.dto.EnvanterAparatiResponse;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.UpdateEnvanterAparatiRequest;

/** Envanterin aparatları; envanter yoksa, silinmişse ya da aparat başka envanterinse 404. */
public interface EnvanterAparatiService {

    EnvanterAparatiResponse getir(Long envanterId, Long id);

    SayfaResponse<EnvanterAparatiResponse> listele(Long envanterId, Pageable sayfa);

    EnvanterAparatiResponse olustur(Long envanterId, CreateEnvanterAparatiRequest istek);

    EnvanterAparatiResponse guncelle(Long envanterId, Long id, UpdateEnvanterAparatiRequest istek);

    void sil(Long envanterId, Long id);
}
