package tr.depo.backend.repository;

import tr.depo.domain.envanter.Envanter;
import tr.depo.domain.tanim.MalzemeTuru;

/** Demirbaş ve sarf malzemeleri aynı tabloda (ENVANTER, TUR ile ayrılır). Filtreli listeler: EnvanterService.ara. */
public interface EnvanterRepository extends AktifKayitRepository<Envanter> {

    /** Silinmemiş kayıtlar arasında (Hibernate filtresi); silinen kaydın numarası yeniden kullanılabilir. */
    boolean existsByTurAndDemirbasNo(MalzemeTuru tur, Long demirbasNo);

    /** Silinmemiş kayıtlar arasında (Hibernate filtresi). */
    boolean existsByTurAndSeriNo(MalzemeTuru tur, String seriNo);
}
