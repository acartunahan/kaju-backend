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

    SayfaResponse<EnvanterHareketiResponse> listele(Pageable sayfa);

    EnvanterHareketiResponse olustur(CreateEnvanterHareketiRequest istek);
}
