package tr.depo.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import tr.depo.domain.envanter.EnvanterHareketi;

public interface EnvanterHareketiRepository extends AktifKayitRepository<EnvanterHareketi> {

    @Override
    @EntityGraph(attributePaths = {"envanter", "neredenDepo", "nereyeDepo", "alanDepo"})
    Page<EnvanterHareketi> findAll(Pageable sayfa);
}
