package tr.depo.backend.kategori;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import tr.depo.backend.common.persistence.AktifKayitRepository;
import tr.depo.domain.tanim.Kategori;

public interface KategoriRepository extends AktifKayitRepository<Kategori> {

    @Override
    @EntityGraph(attributePaths = {"ust", "depo"})
    Page<Kategori> findAll(Pageable sayfa);
}
