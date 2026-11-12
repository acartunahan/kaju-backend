package tr.depo.backend.repository;

import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tr.depo.domain.tanim.Kategori;
import tr.depo.domain.tanim.MalzemeTuru;

public interface KategoriRepository extends AktifKayitRepository<Kategori> {

    @Override
    @EntityGraph(attributePaths = {"ust", "depo"})
    Page<Kategori> findAll(Pageable sayfa);

    /** Silinmemiş kategoriler; null parametre uygulanmaz. {@code adDeseni} kaçışlanmış "içerir" desenidir. */
    @EntityGraph(attributePaths = {"ust", "depo"})
    @Query("select k from Kategori k where k.silinmeZamani is null"
        + " and (:tur is null or k.tur = :tur)"
        + " and (:ustKategoriId is null or k.ust.id = :ustKategoriId)"
        + " and (:adDeseni is null or lower(k.ad) like lower(:adDeseni) escape '!')")
    Page<Kategori> filtrele(@Param("tur") MalzemeTuru tur, @Param("ustKategoriId") Long ustKategoriId,
                            @Param("adDeseni") String adDeseni, Pageable sayfa);

    /** Ağaç için: silinmemiş kategorilerin hepsi (türe göre), ada göre sıralı. */
    @Query("select k from Kategori k where k.silinmeZamani is null and (:tur is null or k.tur = :tur) order by k.ad, k.id")
    List<Kategori> agacIcin(@Param("tur") MalzemeTuru tur);

    /** Verilen kategorilerin doğrudan, silinmemiş alt kategorileri. */
    @Query("select k.id from Kategori k where k.ust.id in :ustIdler and k.silinmeZamani is null")
    List<Long> altKategoriIdleri(@Param("ustIdler") Collection<Long> ustIdler);
}
