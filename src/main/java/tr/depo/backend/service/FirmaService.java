package tr.depo.backend.service;

import org.springframework.data.domain.Pageable;
import tr.depo.backend.dto.CreateFirmaRequest;
import tr.depo.backend.dto.FirmaResponse;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.TurFiltreRequest;
import tr.depo.backend.dto.UpdateFirmaRequest;

public interface FirmaService {

    /** @throws tr.depo.backend.exception.KayitBulunamadiException yoksa ya da silinmişse */
    FirmaResponse getir(Long id);

    /** Silinmemiş firmalar, sayfalı. */
    SayfaResponse<FirmaResponse> listele(TurFiltreRequest filtre, Pageable sayfa);

    FirmaResponse olustur(CreateFirmaRequest istek);

    FirmaResponse guncelle(Long id, UpdateFirmaRequest istek);

    /** Soft delete; aktif bir ihale bu firmayı kullanıyorsa 409. */
    void sil(Long id);
}
