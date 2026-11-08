package tr.depo.backend.support.fixture;

import jakarta.persistence.EntityManager;
import java.util.function.Function;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tr.depo.domain.ortak.BaseEntity;
import tr.depo.domain.tanim.Depo;
import tr.depo.domain.tanim.Marka;
import tr.depo.domain.tanim.Model;

/**
 * Entegrasyon testlerinde API'si henüz olmayan ilişkili kayıtları (depo, model…) doğrudan JPA ile hazırlar.
 * Her işlem kendi transaction'ında çalışır ve commit edilir.
 */
public final class VeritabaniFixture {

    private static final String TEST_SILEN = "TEST";

    private final EntityManager entityManager;
    private final TransactionTemplate islem;

    public VeritabaniFixture(EntityManager entityManager, PlatformTransactionManager islemYoneticisi) {
        this.entityManager = entityManager;
        this.islem = new TransactionTemplate(islemYoneticisi);
    }

    public long depo(String ad) {
        return kaydet(new Depo(ad));
    }

    public long sarfModeli(long markaId, String ad) {
        return islem.execute(durum -> {
            Model model = Model.sarfModeli(entityManager.find(Marka.class, markaId), ad, true);
            entityManager.persist(model);
            entityManager.flush();
            return model.getId();
        });
    }

    public <T extends BaseEntity> void sil(Class<T> tip, long id) {
        islem.executeWithoutResult(durum -> entityManager.find(tip, id).sil(TEST_SILEN));
    }

    public <T extends BaseEntity, R> R oku(Class<T> tip, long id, Function<T, R> okuyucu) {
        return islem.execute(durum -> okuyucu.apply(entityManager.find(tip, id)));
    }

    private long kaydet(BaseEntity kayit) {
        return islem.execute(durum -> {
            entityManager.persist(kayit);
            entityManager.flush();
            return kayit.getId();
        });
    }
}
