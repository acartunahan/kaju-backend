package tr.depo.backend.sayim.satir;

import org.springframework.data.domain.Pageable;
import tr.depo.backend.common.paging.SayfaCevabi;
import tr.depo.backend.sayim.satir.dto.CreateSayimSatiriRequest;
import tr.depo.backend.sayim.satir.dto.SayimSatiriResponse;
import tr.depo.backend.sayim.satir.dto.UpdateSayimSatiriRequest;

/** Sayımın satırları; sayım yoksa, silinmişse ya da satır başka sayımınsa 404. */
public interface SayimSatiriService {

    SayimSatiriResponse getir(Long sayimId, Long id);

    SayfaCevabi<SayimSatiriResponse> listele(Long sayimId, Pageable sayfa);

    SayimSatiriResponse olustur(Long sayimId, CreateSayimSatiriRequest istek);

    SayimSatiriResponse guncelle(Long sayimId, Long id, UpdateSayimSatiriRequest istek);

    void sil(Long sayimId, Long id);
}
