package tr.depo.backend.common.service;

import jakarta.persistence.EntityManager;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;
import tr.depo.domain.ortak.BaseEntity;

/**
 * Soft delete akışının entity'den bağımsız parçaları: silen kullanıcıyı bulmak ve aktif referansları saymak.
 * Servis şablonunun kurucusu küçük kalsın diye tek bileşende toplanmıştır.
 */
@Component
public class SilmeAltyapisi {

    private static final String SAYIM_SORGUSU = "select count(r) from %s r where r.%s = :hedef and r.silinmeZamani is null";

    private final EntityManager entityManager;
    private final AuditorAware<String> denetci;

    public SilmeAltyapisi(EntityManager entityManager, AuditorAware<String> denetci) {
        this.entityManager = entityManager;
        this.denetci = denetci;
    }

    /** Silme işlemini yapan kullanıcı (SILEN kolonu); audit alanlarıyla aynı kaynaktan. */
    public String silen() {
        return denetci.getCurrentAuditor()
            .orElseThrow(() -> new IllegalStateException("Silen kullanıcı belirlenemedi (AuditorAware boş döndü)"));
    }

    /**
     * Hedef kayda referans veren aktif kayıt sayısı. Koşul açıkça yazılır ({@code silinmeZamani is null}); oturumda
     * {@code silinmemis} filtresi kapatılmış olsa da silinmiş referanslar sayılmaz.
     */
    public long aktifReferansSayisi(KullanimReferansi referans, BaseEntity hedef) {
        String entityAdi = entityManager.getMetamodel().entity(referans.kaynak()).getName();
        return entityManager.createQuery(SAYIM_SORGUSU.formatted(entityAdi, referans.alan()), Long.class)
            .setParameter("hedef", hedef)
            .getSingleResult();
    }
}
