package tr.depo.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tr.depo.domain.envanter.ZimmetGecmisi;

public interface ZimmetGecmisiRepository extends AktifKayitRepository<ZimmetGecmisi> {

    /** Bir demirbaşın silinmemiş zimmet geçmişi (Hibernate filtresi). */
    Page<ZimmetGecmisi> findByEnvanter_Id(Long envanterId, Pageable sayfa);
}
