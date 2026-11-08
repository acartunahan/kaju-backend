package tr.depo.backend.service;

import org.springframework.data.domain.Pageable;
import tr.depo.backend.dto.CreateEnvanterNotuRequest;
import tr.depo.backend.dto.EnvanterNotuResponse;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.UpdateEnvanterNotuRequest;

/** Envanterin notları; envanter yoksa, silinmişse ya da not başka envanterinse 404. */
public interface EnvanterNotuService {

    EnvanterNotuResponse getir(Long envanterId, Long id);

    SayfaResponse<EnvanterNotuResponse> listele(Long envanterId, Pageable sayfa);

    EnvanterNotuResponse olustur(Long envanterId, CreateEnvanterNotuRequest istek);

    EnvanterNotuResponse guncelle(Long envanterId, Long id, UpdateEnvanterNotuRequest istek);

    void sil(Long envanterId, Long id);
}
