package tr.depo.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tr.depo.domain.ariza.ArizaIslemi;

public interface ArizaIslemiRepository extends AktifKayitRepository<ArizaIslemi> {

    /** Arızanın silinmemiş işlemleri. */
    Page<ArizaIslemi> findByAriza_Id(Long arizaId, Pageable sayfa);
}
