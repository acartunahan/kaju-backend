package tr.depo.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import tr.depo.domain.tanim.Firma;

public interface FirmaRepository extends AktifKayitRepository<Firma> {

    @Override
    @EntityGraph(attributePaths = "depo")
    Page<Firma> findAll(Pageable sayfa);
}
