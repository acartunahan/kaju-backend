package tr.depo.backend.service;

import org.springframework.data.domain.Pageable;
import tr.depo.backend.dto.CreateMarkaRequest;
import tr.depo.backend.dto.MarkaFiltreRequest;
import tr.depo.backend.dto.MarkaResponse;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.UpdateMarkaRequest;

public interface MarkaService {

    /** @throws tr.depo.backend.exception.KayitBulunamadiException yoksa ya da silinmişse */
    MarkaResponse getir(Long id);

    /** Silinmemiş markalar, sayfalı. */
    SayfaResponse<MarkaResponse> listele(MarkaFiltreRequest filtre, Pageable sayfa);

    MarkaResponse olustur(CreateMarkaRequest istek);

    MarkaResponse guncelle(Long id, UpdateMarkaRequest istek);

    /** Soft delete; aktif model ya da envanter bu markayı kullanıyorsa 409. */
    void sil(Long id);
}
