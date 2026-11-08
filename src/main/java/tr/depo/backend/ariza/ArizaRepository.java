package tr.depo.backend.ariza;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import tr.depo.backend.common.persistence.AktifKayitRepository;
import tr.depo.domain.ariza.Ariza;

public interface ArizaRepository extends AktifKayitRepository<Ariza> {

    @Override
    @EntityGraph(attributePaths = {"envanter", "depo"})
    Page<Ariza> findAll(Pageable sayfa);
}
