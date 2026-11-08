package tr.depo.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import tr.depo.domain.sayim.Sayim;

public interface SayimRepository extends AktifKayitRepository<Sayim> {

    @Override
    @EntityGraph(attributePaths = "depo")
    Page<Sayim> findAll(Pageable sayfa);
}
