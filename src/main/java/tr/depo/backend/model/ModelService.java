package tr.depo.backend.model;

import org.springframework.data.domain.Pageable;
import tr.depo.backend.common.paging.SayfaCevabi;
import tr.depo.backend.model.dto.CreateModelRequest;
import tr.depo.backend.model.dto.ModelResponse;
import tr.depo.backend.model.dto.UpdateModelRequest;

public interface ModelService {

    /** @throws tr.depo.backend.common.exception.KayitBulunamadiException yoksa ya da silinmişse */
    ModelResponse getir(Long id);

    /** Silinmemiş modeller, sayfalı. */
    SayfaCevabi<ModelResponse> listele(Pageable sayfa);

    ModelResponse olustur(CreateModelRequest istek);

    ModelResponse guncelle(Long id, UpdateModelRequest istek);

    /** Soft delete; aktif envanter bu modeli kullanıyorsa 409. */
    void sil(Long id);
}
