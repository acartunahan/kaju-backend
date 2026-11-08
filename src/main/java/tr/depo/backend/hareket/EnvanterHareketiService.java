package tr.depo.backend.hareket;

import org.springframework.data.domain.Pageable;
import tr.depo.backend.common.paging.SayfaCevabi;
import tr.depo.backend.hareket.dto.CreateEnvanterHareketiRequest;
import tr.depo.backend.hareket.dto.EnvanterHareketiResponse;

/**
 * Envanter hareketleri geçmiş kaydıdır: yalnızca okunur ve eklenir. Güncelleme ve silme bilerek yoktur; bir hatayı
 * düzeltmek için düzeltici yeni bir hareket eklenir.
 */
public interface EnvanterHareketiService {

    /** @throws tr.depo.backend.common.exception.KayitBulunamadiException yoksa ya da silinmişse */
    EnvanterHareketiResponse getir(Long id);

    SayfaCevabi<EnvanterHareketiResponse> listele(Pageable sayfa);

    EnvanterHareketiResponse olustur(CreateEnvanterHareketiRequest istek);
}
