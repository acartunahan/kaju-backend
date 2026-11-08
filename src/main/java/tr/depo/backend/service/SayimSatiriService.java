package tr.depo.backend.service;

import org.springframework.data.domain.Pageable;
import tr.depo.backend.dto.CreateSayimSatiriRequest;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.SayimSatiriResponse;
import tr.depo.backend.dto.UpdateSayimSatiriRequest;

/** Sayımın satırları; sayım yoksa, silinmişse ya da satır başka sayımınsa 404. */
public interface SayimSatiriService {

    SayimSatiriResponse getir(Long sayimId, Long id);

    SayfaResponse<SayimSatiriResponse> listele(Long sayimId, Pageable sayfa);

    SayimSatiriResponse olustur(Long sayimId, CreateSayimSatiriRequest istek);

    SayimSatiriResponse guncelle(Long sayimId, Long id, UpdateSayimSatiriRequest istek);

    void sil(Long sayimId, Long id);
}
