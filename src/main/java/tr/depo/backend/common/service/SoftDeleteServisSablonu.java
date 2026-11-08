package tr.depo.backend.common.service;

import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import tr.depo.backend.common.exception.KayitBulunamadiException;
import tr.depo.backend.common.exception.KayitKullanimdaException;
import tr.depo.backend.common.exception.SurumCakismasiException;
import tr.depo.backend.common.persistence.AktifKayitRepository;
import tr.depo.domain.ortak.BaseEntity;

/**
 * Servis implementasyonlarının ortak tabanı (Template Method): silme akışı her entity'de aynıdır
 * (aktif kaydı bul → aktif referans varsa reddet → alt kayıtları sil → kaydı işaretle); entity'ye göre değişen
 * iki adım {@link #kullanimReferanslari()} ve {@link #altKayitlariSil} ile verilir.
 *
 * <p>Her entity'nin kendi {@code XService} arayüzü ve {@code XServiceImpl}'i vardır; bu sınıf yalnızca tekrarı
 * önler ve iş kuralı eklemeye açık kalır. Fiziksel silme hiçbir yolla yapılmaz.
 */
public abstract class SoftDeleteServisSablonu<T extends BaseEntity> {

    private static final Logger log = LoggerFactory.getLogger(SoftDeleteServisSablonu.class);

    private final AktifKayitRepository<T> repository;
    private final SilmeAltyapisi silmeAltyapisi;
    private final String kaynakAdi;

    protected SoftDeleteServisSablonu(AktifKayitRepository<T> repository, SilmeAltyapisi silmeAltyapisi,
                                      String kaynakAdi) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.silmeAltyapisi = Objects.requireNonNull(silmeAltyapisi, "silmeAltyapisi");
        this.kaynakAdi = Objects.requireNonNull(kaynakAdi, "kaynakAdi");
    }

    /**
     * Kaydı soft delete eder; aynı transaction'da alt kayıtlar da silinir.
     *
     * @throws KayitBulunamadiException kayıt yoksa ya da zaten silinmişse (404)
     * @throws KayitKullanimdaException başka aktif kayıtlar referans veriyorsa (409)
     */
    @Transactional
    public void sil(Long id) {
        T kayit = aktifBul(id);
        kullanimdaOlmadiginiDogrula(kayit);
        String silen = silmeAltyapisi.silen();
        altKayitlariSil(kayit, silen);
        kayit.sil(silen);
        log.info("{} silindi (soft delete): id={}", kaynakAdi, id);
    }

    /**
     * Silinmemiş kaydı döndürür; yoksa ya da silinmişse 404. Aynı tabloyu paylaşan kaynaklar (ör. demirbaş / sarf)
     * türü de kontrol etmek için override eder; {@link #sil} de bu metodu kullanır.
     */
    protected T aktifBul(Long id) {
        return repository.findById(id)
            .filter(kayit -> !kayit.isSilindi())
            .orElseThrow(() -> new KayitBulunamadiException(kaynakAdi, id));
    }

    /** PUT'ta istemcinin gönderdiği sürüm kaydın güncel sürümü değilse 409. */
    protected void surumuDogrula(T kayit, int istemcininSurumu) {
        if (kayit.getVersiyon() != istemcininSurumu) {
            throw new SurumCakismasiException(kaynakAdi, kayit.getId());
        }
    }

    /** Bu kayda referans verebilen entity'ler; silmeden önce aktif referans aranır. Varsayılan: yok. */
    protected List<KullanimReferansi> kullanimReferanslari() {
        return List.of();
    }

    /** Aggregate içindeki aktif alt kayıtları {@code sil(silen)} ile işaretler. Varsayılan: alt kayıt yok. */
    protected void altKayitlariSil(T kayit, String silen) {
        // Alt kaydı olan aggregate root'lar override eder.
    }

    protected AktifKayitRepository<T> repository() {
        return repository;
    }

    protected String kaynakAdi() {
        return kaynakAdi;
    }

    private void kullanimdaOlmadiginiDogrula(T kayit) {
        for (KullanimReferansi referans : kullanimReferanslari()) {
            long sayi = silmeAltyapisi.aktifReferansSayisi(referans, kayit);
            if (sayi > 0) {
                throw new KayitKullanimdaException(kaynakAdi, kayit.getId(), referans.kaynakAdi(), sayi);
            }
        }
    }
}
