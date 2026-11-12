package tr.depo.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tr.depo.domain.tanim.MalzemeTuru;
import tr.depo.domain.tanim.Model;

public interface ModelRepository extends AktifKayitRepository<Model> {

    @Override
    @EntityGraph(attributePaths = {"marka", "kategori", "depo"})
    Page<Model> findAll(Pageable sayfa);

    /** Silinmemiş modeller; null parametre uygulanmaz. {@code adDeseni} kaçışlanmış "içerir" desenidir. */
    @EntityGraph(attributePaths = {"marka", "kategori", "depo"})
    @Query("select m from Model m where m.silinmeZamani is null"
        + " and (:tur is null or m.tur = :tur)"
        + " and (:markaId is null or m.marka.id = :markaId)"
        + " and (:kategoriId is null or m.kategori.id = :kategoriId)"
        + " and (:aktif is null or m.aktif = :aktif)"
        + " and (:adDeseni is null or lower(m.ad) like lower(:adDeseni) escape '!')")
    Page<Model> filtrele(@Param("tur") MalzemeTuru tur, @Param("markaId") Long markaId,
                         @Param("kategoriId") Long kategoriId, @Param("aktif") Boolean aktif,
                         @Param("adDeseni") String adDeseni, Pageable sayfa);
}
