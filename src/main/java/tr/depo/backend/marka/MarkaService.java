package tr.depo.backend.marka;

import org.springframework.data.domain.Pageable;
import tr.depo.backend.common.paging.SayfaCevabi;
import tr.depo.backend.marka.dto.CreateMarkaRequest;
import tr.depo.backend.marka.dto.MarkaResponse;
import tr.depo.backend.marka.dto.UpdateMarkaRequest;

public interface MarkaService {

    /** @throws tr.depo.backend.common.exception.KayitBulunamadiException yoksa ya da silinmişse */
    MarkaResponse getir(Long id);

    /** Silinmemiş markalar, sayfalı. */
    SayfaCevabi<MarkaResponse> listele(Pageable sayfa);

    MarkaResponse olustur(CreateMarkaRequest istek);

    MarkaResponse guncelle(Long id, UpdateMarkaRequest istek);

    /** Soft delete; aktif model ya da envanter bu markayı kullanıyorsa 409. */
    void sil(Long id);
}
