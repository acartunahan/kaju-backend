package tr.depo.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tr.depo.domain.envanter.EnvanterHareketi;

public interface EnvanterHareketiRepository extends AktifKayitRepository<EnvanterHareketi> {

    @Override
    @EntityGraph(attributePaths = {"envanter", "neredenDepo", "nereyeDepo", "alanDepo"})
    Page<EnvanterHareketi> findAll(Pageable sayfa);

    /** Bir envanterin silinmemiş hareketleri. */
    @EntityGraph(attributePaths = {"envanter", "neredenDepo", "nereyeDepo", "alanDepo"})
    Page<EnvanterHareketi> findByEnvanter_Id(Long envanterId, Pageable sayfa);

    /** Bir depodan çıkan, depoya giren ya da deponun aldığı (sarf) silinmemiş hareketler. */
    @EntityGraph(attributePaths = {"envanter", "neredenDepo", "nereyeDepo", "alanDepo"})
    @Query(value = "select h from EnvanterHareketi h where h.silinmeZamani is null "
        + "and (h.neredenDepo.id = :depoId or h.nereyeDepo.id = :depoId or h.alanDepo.id = :depoId)",
        countQuery = "select count(h) from EnvanterHareketi h where h.silinmeZamani is null "
            + "and (h.neredenDepo.id = :depoId or h.nereyeDepo.id = :depoId or h.alanDepo.id = :depoId)")
    Page<EnvanterHareketi> deponunHareketleri(@Param("depoId") Long depoId, Pageable sayfa);
}
