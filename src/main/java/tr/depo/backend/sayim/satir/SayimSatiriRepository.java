package tr.depo.backend.sayim.satir;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import tr.depo.backend.common.persistence.AktifKayitRepository;
import tr.depo.domain.sayim.SayimSatiri;

public interface SayimSatiriRepository extends AktifKayitRepository<SayimSatiri> {

    /** Sayımın silinmemiş satırları; cevapta özetlenen envanter ve depo birlikte okunur. */
    @EntityGraph(attributePaths = {"envanter", "depo"})
    Page<SayimSatiri> findBySayim_Id(Long sayimId, Pageable sayfa);
}
