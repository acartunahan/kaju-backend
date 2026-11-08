package tr.depo.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tr.depo.domain.envanter.Barkod;

public interface BarkodRepository extends AktifKayitRepository<Barkod> {

    /** Envanterin silinmemiş barkodları. */
    Page<Barkod> findByEnvanter_Id(Long envanterId, Pageable sayfa);
}
