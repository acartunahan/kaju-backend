package tr.depo.backend.depo;

import org.springframework.data.domain.Pageable;
import tr.depo.backend.common.paging.SayfaCevabi;
import tr.depo.backend.depo.dto.CreateDepoRequest;
import tr.depo.backend.depo.dto.DepoResponse;
import tr.depo.backend.depo.dto.UpdateDepoRequest;

public interface DepoService {

    /** @throws tr.depo.backend.common.exception.KayitBulunamadiException yoksa ya da silinmişse */
    DepoResponse getir(Long id);

    /** Silinmemiş depolar, sayfalı. */
    SayfaCevabi<DepoResponse> listele(Pageable sayfa);

    DepoResponse olustur(CreateDepoRequest istek);

    DepoResponse guncelle(Long id, UpdateDepoRequest istek);

    /** Soft delete; depoya bağlı aktif herhangi bir kayıt varsa 409. */
    void sil(Long id);
}
