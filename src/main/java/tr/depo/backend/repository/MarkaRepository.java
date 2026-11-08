package tr.depo.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tr.depo.domain.tanim.MalzemeTuru;
import tr.depo.domain.tanim.Marka;

public interface MarkaRepository extends AktifKayitRepository<Marka> {

    /** Depo özeti cevapta döndüğü için liste depoyla birlikte okunur (N+1 yok; to-one fetch sayfalamayla uyumlu). */
    @Override
    @EntityGraph(attributePaths = "depo")
    Page<Marka> findAll(Pageable sayfa);

    /** Silinmemiş markalar; null parametre uygulanmaz. {@code adDeseni} kaçışlanmış "içerir" desenidir. */
    @EntityGraph(attributePaths = "depo")
    @Query("select m from Marka m where m.silinmeZamani is null"
        + " and (:tur is null or m.tur = :tur)"
        + " and (:aktif is null or m.aktif = :aktif)"
        + " and (:adDeseni is null or lower(m.ad) like lower(:adDeseni) escape '!')")
    Page<Marka> filtrele(@Param("tur") MalzemeTuru tur, @Param("aktif") Boolean aktif,
                         @Param("adDeseni") String adDeseni, Pageable sayfa);
}
