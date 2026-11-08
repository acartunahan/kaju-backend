package tr.depo.backend.common.service;

import jakarta.persistence.EntityManager;
import java.util.List;
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
    private static final String ALT_KAYIT_SORGUSU = "select a from %s a where a.%s = :ust and a.silinmeZamani is null";

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
     * Aggregate root silinirken alt kayıtlarını aynı transaction'da soft delete eder. Her kayıt entity üzerinden
     * işaretlenir (toplu UPDATE değil): sürüm ve audit alanları da güncellenir.
     *
     * @return silinen alt kayıt sayısı
     */
    public int aktifAltKayitlariSil(Class<? extends BaseEntity> tip, String ustAlani, BaseEntity ust, String silen) {
        String entityAdi = entityManager.getMetamodel().entity(tip).getName();
        List<? extends BaseEntity> altKayitlar = entityManager
            .createQuery(ALT_KAYIT_SORGUSU.formatted(entityAdi, ustAlani), tip)
            .setParameter("ust", ust)
            .getResultList();
        altKayitlar.forEach(altKayit -> altKayit.sil(silen));
        return altKayitlar.size();
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
