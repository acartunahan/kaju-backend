package tr.depo.backend.service;

import org.springframework.data.domain.Pageable;
import tr.depo.backend.dto.CreateModelRequest;
import tr.depo.backend.dto.ModelFiltreRequest;
import tr.depo.backend.dto.ModelResponse;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.UpdateModelRequest;

public interface ModelService {

    /** @throws tr.depo.backend.exception.KayitBulunamadiException yoksa ya da silinmişse */
    ModelResponse getir(Long id);

    /** Silinmemiş modeller, sayfalı. */
    SayfaResponse<ModelResponse> listele(ModelFiltreRequest filtre, Pageable sayfa);

    ModelResponse olustur(CreateModelRequest istek);

    ModelResponse guncelle(Long id, UpdateModelRequest istek);

    /** Soft delete; aktif envanter bu modeli kullanıyorsa 409. */
    void sil(Long id);
}
