package tr.depo.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tr.depo.domain.tanim.Firma;
import tr.depo.domain.tanim.MalzemeTuru;

public interface FirmaRepository extends AktifKayitRepository<Firma> {

    @Override
    @EntityGraph(attributePaths = "depo")
    Page<Firma> findAll(Pageable sayfa);

    /** Silinmemiş firmalar; {@code tur} null ise uygulanmaz. */
    @EntityGraph(attributePaths = "depo")
    @Query("select f from Firma f where f.silinmeZamani is null and (:tur is null or f.tur = :tur)")
    Page<Firma> filtrele(@Param("tur") MalzemeTuru tur, Pageable sayfa);
}
