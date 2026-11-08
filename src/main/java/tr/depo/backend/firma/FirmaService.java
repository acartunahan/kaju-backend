package tr.depo.backend.firma;

import org.springframework.data.domain.Pageable;
import tr.depo.backend.common.paging.SayfaCevabi;
import tr.depo.backend.firma.dto.CreateFirmaRequest;
import tr.depo.backend.firma.dto.FirmaResponse;
import tr.depo.backend.firma.dto.UpdateFirmaRequest;

public interface FirmaService {

    /** @throws tr.depo.backend.common.exception.KayitBulunamadiException yoksa ya da silinmişse */
    FirmaResponse getir(Long id);

    /** Silinmemiş firmalar, sayfalı. */
    SayfaCevabi<FirmaResponse> listele(Pageable sayfa);

    FirmaResponse olustur(CreateFirmaRequest istek);

    FirmaResponse guncelle(Long id, UpdateFirmaRequest istek);

    /** Soft delete; aktif bir ihale bu firmayı kullanıyorsa 409. */
    void sil(Long id);
}
