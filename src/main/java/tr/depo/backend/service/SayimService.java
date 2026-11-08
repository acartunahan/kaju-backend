package tr.depo.backend.service;

import org.springframework.data.domain.Pageable;
import tr.depo.backend.dto.CreateSayimRequest;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.SayimResponse;
import tr.depo.backend.dto.UpdateSayimRequest;

public interface SayimService {

    /** @throws tr.depo.backend.exception.KayitBulunamadiException yoksa ya da silinmişse */
    SayimResponse getir(Long id);

    /** Silinmemiş sayımlar, sayfalı. */
    SayfaResponse<SayimResponse> listele(Pageable sayfa);

    SayimResponse olustur(CreateSayimRequest istek);

    SayimResponse guncelle(Long id, UpdateSayimRequest istek);

    /** Soft delete; sayım satırları da aynı transaction'da silinir. */
    void sil(Long id);
}
