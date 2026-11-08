package tr.depo.backend.service;

import org.springframework.data.domain.Pageable;
import tr.depo.backend.dto.CreateDepoRequest;
import tr.depo.backend.dto.DepoResponse;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.UpdateDepoRequest;

public interface DepoService {

    /** @throws tr.depo.backend.exception.KayitBulunamadiException yoksa ya da silinmişse */
    DepoResponse getir(Long id);

    /** Silinmemiş depolar, sayfalı. */
    SayfaResponse<DepoResponse> listele(Pageable sayfa);

    DepoResponse olustur(CreateDepoRequest istek);

    DepoResponse guncelle(Long id, UpdateDepoRequest istek);

    /** Soft delete; depoya bağlı aktif herhangi bir kayıt varsa 409. */
    void sil(Long id);
}
