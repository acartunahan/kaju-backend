package tr.depo.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import tr.depo.domain.envanter.ZimmetKaydi;

public interface ZimmetKaydiRepository extends AktifKayitRepository<ZimmetKaydi> {

    @Override
    @EntityGraph(attributePaths = "depo")
    Page<ZimmetKaydi> findAll(Pageable sayfa);
}
