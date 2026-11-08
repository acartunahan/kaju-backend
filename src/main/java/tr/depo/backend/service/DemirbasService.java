package tr.depo.backend.service;

import org.springframework.data.domain.Pageable;
import tr.depo.backend.dto.CreateDemirbasRequest;
import tr.depo.backend.dto.DemirbasResponse;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.UpdateDemirbasRequest;

public interface DemirbasService {

    /** @throws tr.depo.backend.exception.KayitBulunamadiException yoksa, silinmişse ya da demirbaş değilse */
    DemirbasResponse getir(Long id);

    /** Silinmemiş demirbaşlar, sayfalı. */
    SayfaResponse<DemirbasResponse> listele(Pageable sayfa);

    /**
     * Bir sicile şu an zimmetli olan silinmemiş demirbaşlar, sayfalı. Sicil olduğu gibi (baştaki/sondaki boşluklar
     * atılarak) aranır; biçimi bilinmediği için baştaki sıfırlar eklenmez/silinmez.
     *
     * @throws tr.depo.backend.exception.GecersizIstekException sicil boşsa (400)
     */
    SayfaResponse<DemirbasResponse> zimmetliListele(String zimmetliSicil, Pageable sayfa);

    DemirbasResponse olustur(CreateDemirbasRequest istek);

    DemirbasResponse guncelle(Long id, UpdateDemirbasRequest istek);

    /**
     * Soft delete; barkod, not, aparat ve fotoğraflar da aynı transaction'da silinir. Hareket, arıza ve sayım satırları
     * geçmiş kaydıdır, silinmez.
     */
    void sil(Long id);
}
