package tr.depo.backend.envanter;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import tr.depo.backend.common.persistence.AktifKayitRepository;
import tr.depo.domain.envanter.Envanter;
import tr.depo.domain.tanim.MalzemeTuru;

/** Demirbaş ve sarf malzemeleri aynı tabloda (ENVANTER, TUR ile ayrılır); listeler türe göre alınır. */
public interface EnvanterRepository extends AktifKayitRepository<Envanter> {

    /** Cevapta özetlenen to-one ilişkiler birlikte okunur (N+1 yok). Silinmemişler (Hibernate filtresi). */
    @EntityGraph(attributePaths = {"kategori", "marka", "model", "depo", "ihale"})
    Page<Envanter> findByTur(MalzemeTuru tur, Pageable sayfa);
}
