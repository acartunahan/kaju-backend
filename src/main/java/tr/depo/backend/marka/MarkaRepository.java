package tr.depo.backend.marka;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import tr.depo.backend.common.persistence.AktifKayitRepository;
import tr.depo.domain.tanim.Marka;

public interface MarkaRepository extends AktifKayitRepository<Marka> {

    /** Depo özeti cevapta döndüğü için liste depoyla birlikte okunur (N+1 yok; to-one fetch sayfalamayla uyumlu). */
    @Override
    @EntityGraph(attributePaths = "depo")
    Page<Marka> findAll(Pageable sayfa);
}
