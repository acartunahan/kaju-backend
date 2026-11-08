package tr.depo.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import tr.depo.domain.tanim.Model;

public interface ModelRepository extends AktifKayitRepository<Model> {

    @Override
    @EntityGraph(attributePaths = {"marka", "kategori", "depo"})
    Page<Model> findAll(Pageable sayfa);
}
