package tr.depo.backend.zimmet;

import org.springframework.data.domain.Pageable;
import tr.depo.backend.common.paging.SayfaCevabi;
import tr.depo.backend.zimmet.dto.CreateZimmetKaydiRequest;
import tr.depo.backend.zimmet.dto.ZimmetKaydiResponse;

/** Sarf zimmet/düşüm günlüğü: geçmiş kaydı, yalnızca okunur ve eklenir. */
public interface ZimmetKaydiService {

    /** @throws tr.depo.backend.common.exception.KayitBulunamadiException yoksa ya da silinmişse */
    ZimmetKaydiResponse getir(Long id);

    SayfaCevabi<ZimmetKaydiResponse> listele(Pageable sayfa);

    ZimmetKaydiResponse olustur(CreateZimmetKaydiRequest istek);
}
