package tr.depo.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import tr.depo.domain.ariza.Ariza;

public interface ArizaRepository extends AktifKayitRepository<Ariza> {

    @Override
    @EntityGraph(attributePaths = {"envanter", "depo"})
    Page<Ariza> findAll(Pageable sayfa);

    /** Bir envanterin silinmemiş arızaları. */
    @EntityGraph(attributePaths = {"envanter", "depo"})
    Page<Ariza> findByEnvanter_Id(Long envanterId, Pageable sayfa);
}
