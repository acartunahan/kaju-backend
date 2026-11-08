package tr.depo.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tr.depo.domain.envanter.EnvanterAparati;

public interface EnvanterAparatiRepository extends AktifKayitRepository<EnvanterAparati> {

    /** Envanterin silinmemiş aparatları. */
    Page<EnvanterAparati> findByEnvanter_Id(Long envanterId, Pageable sayfa);
}
