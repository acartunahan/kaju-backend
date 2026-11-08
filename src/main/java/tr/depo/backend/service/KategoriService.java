package tr.depo.backend.service;

import java.util.List;
import org.springframework.data.domain.Pageable;
import tr.depo.backend.dto.CreateKategoriRequest;
import tr.depo.backend.dto.KategoriAgacResponse;
import tr.depo.backend.dto.KategoriFiltreRequest;
import tr.depo.backend.dto.KategoriResponse;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.TurFiltreRequest;
import tr.depo.backend.dto.UpdateKategoriRequest;

public interface KategoriService {

    /** @throws tr.depo.backend.exception.KayitBulunamadiException yoksa ya da silinmişse */
    KategoriResponse getir(Long id);

    /** Silinmemiş kategoriler, sayfalı (düz liste; ağaç sorguları ayrıca eklenecek). */
    SayfaResponse<KategoriResponse> listele(KategoriFiltreRequest filtre, Pageable sayfa);

    /**
     * Silinmemiş kategoriler ağaç olarak ({@code tur} verilirse yalnızca o türden). Üst kategorisi olmayan ya da
     * silinmiş olanlar köktür; ağaçta döngü olan legacy kayıtlar da kaybolmaz (döngü kırılarak köke eklenir).
     */
    List<KategoriAgacResponse> agac(TurFiltreRequest filtre);

    KategoriResponse olustur(CreateKategoriRequest istek);

    KategoriResponse guncelle(Long id, UpdateKategoriRequest istek);

    /** Soft delete; aktif alt kategori, model ya da envanter varsa 409 (alt kategoriler birlikte silinmez). */
    void sil(Long id);
}
