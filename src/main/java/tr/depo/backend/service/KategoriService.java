package tr.depo.backend.service;

import org.springframework.data.domain.Pageable;
import tr.depo.backend.dto.CreateKategoriRequest;
import tr.depo.backend.dto.KategoriResponse;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.UpdateKategoriRequest;

public interface KategoriService {

    /** @throws tr.depo.backend.exception.KayitBulunamadiException yoksa ya da silinmişse */
    KategoriResponse getir(Long id);

    /** Silinmemiş kategoriler, sayfalı (düz liste; ağaç sorguları ayrıca eklenecek). */
    SayfaResponse<KategoriResponse> listele(Pageable sayfa);

    KategoriResponse olustur(CreateKategoriRequest istek);

    KategoriResponse guncelle(Long id, UpdateKategoriRequest istek);

    /** Soft delete; aktif alt kategori, model ya da envanter varsa 409 (alt kategoriler birlikte silinmez). */
    void sil(Long id);
}
