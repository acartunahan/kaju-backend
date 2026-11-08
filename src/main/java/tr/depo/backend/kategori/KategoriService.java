package tr.depo.backend.kategori;

import org.springframework.data.domain.Pageable;
import tr.depo.backend.common.paging.SayfaCevabi;
import tr.depo.backend.kategori.dto.CreateKategoriRequest;
import tr.depo.backend.kategori.dto.KategoriResponse;
import tr.depo.backend.kategori.dto.UpdateKategoriRequest;

public interface KategoriService {

    /** @throws tr.depo.backend.common.exception.KayitBulunamadiException yoksa ya da silinmişse */
    KategoriResponse getir(Long id);

    /** Silinmemiş kategoriler, sayfalı (düz liste; ağaç sorguları ayrıca eklenecek). */
    SayfaCevabi<KategoriResponse> listele(Pageable sayfa);

    KategoriResponse olustur(CreateKategoriRequest istek);

    KategoriResponse guncelle(Long id, UpdateKategoriRequest istek);

    /** Soft delete; aktif alt kategori, model ya da envanter varsa 409 (alt kategoriler birlikte silinmez). */
    void sil(Long id);
}
