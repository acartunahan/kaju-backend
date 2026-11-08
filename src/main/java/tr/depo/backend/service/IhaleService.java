package tr.depo.backend.service;

import org.springframework.data.domain.Pageable;
import tr.depo.backend.dto.CreateIhaleRequest;
import tr.depo.backend.dto.IhaleResponse;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.UpdateIhaleRequest;

public interface IhaleService {

    /** @throws tr.depo.backend.exception.KayitBulunamadiException yoksa ya da silinmişse */
    IhaleResponse getir(Long id);

    /** Silinmemiş ihaleler, sayfalı. */
    SayfaResponse<IhaleResponse> listele(Pageable sayfa);

    IhaleResponse olustur(CreateIhaleRequest istek);

    IhaleResponse guncelle(Long id, UpdateIhaleRequest istek);

    /** Soft delete; aktif envanter bu ihaleyi kullanıyorsa 409. */
    void sil(Long id);
}
