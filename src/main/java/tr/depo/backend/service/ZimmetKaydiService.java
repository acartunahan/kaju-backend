package tr.depo.backend.service;

import org.springframework.data.domain.Pageable;
import tr.depo.backend.dto.CreateZimmetKaydiRequest;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.ZimmetKaydiResponse;

/** Sarf zimmet/düşüm günlüğü: geçmiş kaydı, yalnızca okunur ve eklenir. */
public interface ZimmetKaydiService {

    /** @throws tr.depo.backend.exception.KayitBulunamadiException yoksa ya da silinmişse */
    ZimmetKaydiResponse getir(Long id);

    SayfaResponse<ZimmetKaydiResponse> listele(Pageable sayfa);

    ZimmetKaydiResponse olustur(CreateZimmetKaydiRequest istek);
}
