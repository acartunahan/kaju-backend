package tr.depo.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import tr.depo.domain.envanter.Envanter;
import tr.depo.domain.tanim.MalzemeTuru;

/** Demirbaş ve sarf malzemeleri aynı tabloda (ENVANTER, TUR ile ayrılır); listeler türe göre alınır. */
public interface EnvanterRepository extends AktifKayitRepository<Envanter> {

    /** Cevapta özetlenen to-one ilişkiler birlikte okunur (N+1 yok). Silinmemişler (Hibernate filtresi). */
    @EntityGraph(attributePaths = {"kategori", "marka", "model", "depo", "ihale"})
    Page<Envanter> findByTur(MalzemeTuru tur, Pageable sayfa);

    /** Bir sicile zimmetli olanlar (ZIMMETLI_SICIL tam eşleşme). Silinmemişler (Hibernate filtresi). */
    @EntityGraph(attributePaths = {"kategori", "marka", "model", "depo", "ihale"})
    Page<Envanter> findByTurAndZimmetliSicil_Deger(MalzemeTuru tur, String zimmetliSicil, Pageable sayfa);

    /** Silinmemiş kayıtlar arasında (Hibernate filtresi); silinen kaydın numarası yeniden kullanılabilir. */
    boolean existsByTurAndDemirbasNo(MalzemeTuru tur, Long demirbasNo);

    /** Silinmemiş kayıtlar arasında (Hibernate filtresi). */
    boolean existsByTurAndSeriNo(MalzemeTuru tur, String seriNo);
}
