package tr.depo.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import tr.depo.domain.tanim.Ihale;

public interface IhaleRepository extends AktifKayitRepository<Ihale> {

    @Override
    @EntityGraph(attributePaths = "firma")
    Page<Ihale> findAll(Pageable sayfa);
}
