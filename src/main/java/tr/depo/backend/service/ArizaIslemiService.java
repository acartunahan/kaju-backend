package tr.depo.backend.service;

import org.springframework.data.domain.Pageable;
import tr.depo.backend.dto.ArizaIslemiResponse;
import tr.depo.backend.dto.CreateArizaIslemiRequest;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.UpdateArizaIslemiRequest;

/** Arızanın işlem geçmişi; arıza yoksa, silinmişse ya da işlem başka arızanınsa 404. */
public interface ArizaIslemiService {

    ArizaIslemiResponse getir(Long arizaId, Long id);

    SayfaResponse<ArizaIslemiResponse> listele(Long arizaId, Pageable sayfa);

    ArizaIslemiResponse olustur(Long arizaId, CreateArizaIslemiRequest istek);

    ArizaIslemiResponse guncelle(Long arizaId, Long id, UpdateArizaIslemiRequest istek);

    void sil(Long arizaId, Long id);
}
