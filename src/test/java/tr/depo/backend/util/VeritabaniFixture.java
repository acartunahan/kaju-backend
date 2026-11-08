package tr.depo.backend.util;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.function.Consumer;
import java.util.function.Function;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tr.depo.domain.envanter.DemirbasDurumu;
import tr.depo.domain.envanter.Envanter;
import tr.depo.domain.envanter.EnvanterHareketi;
import tr.depo.domain.ortak.BaseEntity;
import tr.depo.domain.tanim.Depo;
import tr.depo.domain.tanim.Kategori;
import tr.depo.domain.tanim.MalzemeTuru;
import tr.depo.domain.tanim.Marka;
import tr.depo.domain.tanim.Model;

/**
 * Entegrasyon testlerinde API'si henüz olmayan ilişkili kayıtları (depo, model, envanter…) doğrudan JPA ile
 * hazırlar. Her işlem kendi transaction'ında çalışır ve commit edilir.
 */
public final class VeritabaniFixture {

    private static final String TEST_SILEN = "TEST";
    private static final LocalDateTime KAYIT_TARIHI = LocalDateTime.of(2026, 1, 1, 0, 0);

    private final EntityManager entityManager;
    private final TransactionTemplate islem;

    public VeritabaniFixture(EntityManager entityManager, PlatformTransactionManager islemYoneticisi) {
        this.entityManager = entityManager;
        this.islem = new TransactionTemplate(islemYoneticisi);
    }

    public long depo(String ad) {
        return kaydet(new Depo(ad));
    }

    public long marka(MalzemeTuru tur, String ad) {
        return kaydet(new Marka(tur, ad));
    }

    public long demirbasModeli(long markaId, String ad) {
        return islem.execute(durum -> {
            Model model = Model.demirbasModeli(ad);
            model.setMarka(entityManager.find(Marka.class, markaId));
            entityManager.persist(model);
            entityManager.flush();
            return model.getId();
        });
    }

    public long sarfModeli(long markaId, String ad) {
        return islem.execute(durum -> {
            Model model = Model.sarfModeli(entityManager.find(Marka.class, markaId), ad, true);
            entityManager.persist(model);
            entityManager.flush();
            return model.getId();
        });
    }

    /** Ürün oluşturmak için zorunlu demirbaş tanımları: kategori, marka ve o markanın modeli. */
    public TanimSeti demirbasTanimlari(String ad) {
        long kategoriId = kaydet(Kategori.demirbasKategorisi(ad + " Kategorisi", "1"));
        long markaId = marka(MalzemeTuru.DEMIRBAS, ad + " Markası");
        return new TanimSeti(kategoriId, markaId, demirbasModeli(markaId, ad + " Modeli"));
    }

    /** Ürün oluşturmak için zorunlu sarf tanımları: kategori, marka ve o markanın modeli. */
    public TanimSeti sarfTanimlari(String ad) {
        long kategoriId = kaydet(Kategori.sarfKategorisi(ad + " Kategorisi"));
        long markaId = marka(MalzemeTuru.SARF, ad + " Markası");
        return new TanimSeti(kategoriId, markaId, sarfModeli(markaId, ad + " Modeli"));
    }

    /** Demirbaş envanter; {@code ayar} ile ilişkiler bağlanır (ör. {@code e -> e.setDepo(bul(Depo.class, id))}). */
    public long demirbasEnvanter(Consumer<Envanter> ayar) {
        return envanter(Envanter.demirbas(DemirbasDurumu.NORMAL, KAYIT_TARIHI), ayar);
    }

    public long sarfEnvanter(String ad, Consumer<Envanter> ayar) {
        return envanter(Envanter.sarf(ad), ayar);
    }

    /** Envantere bağlı bir demirbaş hareketi (geçmiş kaydı); envanter silinince silinmemesi gereken kayıt. */
    public long demirbasHareketi(long envanterId) {
        return islem.execute(durum -> {
            EnvanterHareketi hareket = EnvanterHareketi.demirbasHareketi(BigDecimal.ONE);
            hareket.setEnvanter(entityManager.find(Envanter.class, envanterId));
            hareket.setHareketAdi("Transfer");
            entityManager.persist(hareket);
            entityManager.flush();
            return hareket.getId();
        });
    }

    /** {@link #demirbasEnvanter} / {@link #sarfEnvanter} ayarlarında ilişkili kaydı yüklemek için. */
    public <T extends BaseEntity> T bul(Class<T> tip, long id) {
        return entityManager.find(tip, id);
    }

    public <T extends BaseEntity> void sil(Class<T> tip, long id) {
        islem.executeWithoutResult(durum -> entityManager.find(tip, id).sil(TEST_SILEN));
    }

    public <T extends BaseEntity, R> R oku(Class<T> tip, long id, Function<T, R> okuyucu) {
        return islem.execute(durum -> okuyucu.apply(entityManager.find(tip, id)));
    }

    private long envanter(Envanter envanter, Consumer<Envanter> ayar) {
        return islem.execute(durum -> {
            ayar.accept(envanter);
            entityManager.persist(envanter);
            entityManager.flush();
            return envanter.getId();
        });
    }

    private long kaydet(BaseEntity kayit) {
        return islem.execute(durum -> {
            entityManager.persist(kayit);
            entityManager.flush();
            return kayit.getId();
        });
    }
}
