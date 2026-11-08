package tr.depo.backend.ariza.islem;

import org.springframework.data.domain.Pageable;
import tr.depo.backend.ariza.islem.dto.ArizaIslemiResponse;
import tr.depo.backend.ariza.islem.dto.CreateArizaIslemiRequest;
import tr.depo.backend.ariza.islem.dto.UpdateArizaIslemiRequest;
import tr.depo.backend.common.paging.SayfaCevabi;

/** Arızanın işlem geçmişi; arıza yoksa, silinmişse ya da işlem başka arızanınsa 404. */
public interface ArizaIslemiService {

    ArizaIslemiResponse getir(Long arizaId, Long id);

    SayfaCevabi<ArizaIslemiResponse> listele(Long arizaId, Pageable sayfa);

    ArizaIslemiResponse olustur(Long arizaId, CreateArizaIslemiRequest istek);

    ArizaIslemiResponse guncelle(Long arizaId, Long id, UpdateArizaIslemiRequest istek);

    void sil(Long arizaId, Long id);
}
