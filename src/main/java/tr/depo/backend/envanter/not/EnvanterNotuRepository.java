package tr.depo.backend.envanter.not;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tr.depo.backend.common.persistence.AktifKayitRepository;
import tr.depo.domain.envanter.EnvanterNotu;

public interface EnvanterNotuRepository extends AktifKayitRepository<EnvanterNotu> {

    /** Envanterin silinmemiş notları. */
    Page<EnvanterNotu> findByEnvanter_Id(Long envanterId, Pageable sayfa);
}
