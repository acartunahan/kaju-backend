package tr.depo.backend.service;

import java.util.List;
import org.springframework.data.domain.Pageable;
import tr.depo.backend.dto.CreateDemirbasRequest;
import tr.depo.backend.dto.DemirbasFiltreRequest;
import tr.depo.backend.dto.DemirbasResponse;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.UpdateDemirbasRequest;

public interface DemirbasService {

    /** @throws tr.depo.backend.exception.KayitBulunamadiException yoksa, silinmişse ya da demirbaş değilse */
    DemirbasResponse getir(Long id);

    /**
     * Silinmemiş demirbaşlar, sayfalı; filtrelerin hepsi birlikte uygulanır (bkz. {@link DemirbasFiltreRequest}).
     * Zimmetli sicil olduğu gibi (baştaki/sondaki boşluklar atılarak) aranır; biçimi bilinmediği için baştaki sıfırlar
     * eklenmez/silinmez.
     *
     * @throws tr.depo.backend.exception.GecersizIstekException zimmetliSicil boş verilirse (400)
     */
    SayfaResponse<DemirbasResponse> listele(DemirbasFiltreRequest filtre, Pageable sayfa);

    /**
     * Barkod okutma / numarayla bulma: barkod, seri no ya da demirbaş no ile tam eşleşen silinmemiş demirbaşlar.
     * Legacy'de bu değerler tekrarlı olabildiği için liste döner (en fazla 100).
     *
     * @throws tr.depo.backend.exception.GecersizIstekException üçünden tam biri verilmezse (400)
     */
    List<DemirbasResponse> bul(String barkod, String seriNo, Long demirbasNo);

    DemirbasResponse olustur(CreateDemirbasRequest istek);

    DemirbasResponse guncelle(Long id, UpdateDemirbasRequest istek);

    /**
     * Soft delete; barkod, not, aparat ve fotoğraflar da aynı transaction'da silinir. Hareket, arıza ve sayım satırları
     * geçmiş kaydıdır, silinmez.
     */
    void sil(Long id);
}
