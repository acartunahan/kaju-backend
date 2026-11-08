package tr.depo.backend.ariza;

import org.springframework.data.domain.Pageable;
import tr.depo.backend.ariza.dto.ArizaResponse;
import tr.depo.backend.ariza.dto.CreateArizaRequest;
import tr.depo.backend.ariza.dto.UpdateArizaRequest;
import tr.depo.backend.common.paging.SayfaCevabi;

public interface ArizaService {

    /** @throws tr.depo.backend.common.exception.KayitBulunamadiException yoksa ya da silinmişse */
    ArizaResponse getir(Long id);

    /** Silinmemiş arızalar, sayfalı. */
    SayfaCevabi<ArizaResponse> listele(Pageable sayfa);

    ArizaResponse olustur(CreateArizaRequest istek);

    ArizaResponse guncelle(Long id, UpdateArizaRequest istek);

    /** Soft delete; arıza işlemleri de aynı transaction'da silinir. */
    void sil(Long id);
}
