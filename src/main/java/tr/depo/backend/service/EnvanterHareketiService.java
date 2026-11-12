package tr.depo.backend.service;

import org.springframework.data.domain.Pageable;
import tr.depo.backend.dto.CreateEnvanterHareketiRequest;
import tr.depo.backend.dto.EnvanterHareketiResponse;
import tr.depo.backend.dto.SayfaResponse;

/**
 * Envanter hareketleri geçmiş kaydıdır: yalnızca okunur ve eklenir. Güncelleme ve silme bilerek yoktur; bir hatayı
 * düzeltmek için düzeltici yeni bir hareket eklenir.
 */
public interface EnvanterHareketiService {

    /** @throws tr.depo.backend.exception.KayitBulunamadiException yoksa ya da silinmişse */
    EnvanterHareketiResponse getir(Long id);

    /** Silinmemiş hareketler, sayfalı. {@code depoId} verilirse o depodan çıkan, o depoya giren ya da o deponun aldığı hareketler. */
    SayfaResponse<EnvanterHareketiResponse> listele(Long depoId, Pageable sayfa);

    /**
     * Bir demirbaşın hareketleri, sayfalı; sıralama verilmezse en yeni önce.
     *
     * @throws tr.depo.backend.exception.KayitBulunamadiException demirbaş yoksa, silinmişse ya da sarf ise (404)
     */
    SayfaResponse<EnvanterHareketiResponse> demirbasHareketleri(Long demirbasId, Pageable sayfa);

    /**
     * Bir sarf malzemesinin stok hareketleri, sayfalı; sıralama verilmezse en yeni önce.
     *
     * @throws tr.depo.backend.exception.KayitBulunamadiException sarf malzemesi yoksa, silinmişse ya da demirbaş ise (404)
     */
    SayfaResponse<EnvanterHareketiResponse> sarfHareketleri(Long sarfMalzemesiId, Pageable sayfa);

    EnvanterHareketiResponse olustur(CreateEnvanterHareketiRequest istek);
}
