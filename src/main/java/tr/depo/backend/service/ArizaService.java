package tr.depo.backend.service;

import org.springframework.data.domain.Pageable;
import tr.depo.backend.dto.ArizaResponse;
import tr.depo.backend.dto.CreateArizaRequest;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.UpdateArizaRequest;

public interface ArizaService {

    /** @throws tr.depo.backend.exception.KayitBulunamadiException yoksa ya da silinmişse */
    ArizaResponse getir(Long id);

    /** Silinmemiş arızalar, sayfalı. */
    SayfaResponse<ArizaResponse> listele(Pageable sayfa);

    ArizaResponse olustur(CreateArizaRequest istek);

    ArizaResponse guncelle(Long id, UpdateArizaRequest istek);

    /** Soft delete; arıza işlemleri de aynı transaction'da silinir. */
    void sil(Long id);
}
