package tr.depo.backend.ihale;

import org.springframework.data.domain.Pageable;
import tr.depo.backend.common.paging.SayfaCevabi;
import tr.depo.backend.ihale.dto.CreateIhaleRequest;
import tr.depo.backend.ihale.dto.IhaleResponse;
import tr.depo.backend.ihale.dto.UpdateIhaleRequest;

public interface IhaleService {

    /** @throws tr.depo.backend.common.exception.KayitBulunamadiException yoksa ya da silinmişse */
    IhaleResponse getir(Long id);

    /** Silinmemiş ihaleler, sayfalı. */
    SayfaCevabi<IhaleResponse> listele(Pageable sayfa);

    IhaleResponse olustur(CreateIhaleRequest istek);

    IhaleResponse guncelle(Long id, UpdateIhaleRequest istek);

    /** Soft delete; aktif envanter bu ihaleyi kullanıyorsa 409. */
    void sil(Long id);
}
