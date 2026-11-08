package tr.depo.backend.serviceimpl;

import java.util.Objects;
import org.springframework.transaction.annotation.Transactional;
import tr.depo.backend.exception.KayitBulunamadiException;
import tr.depo.backend.repository.AktifKayitRepository;
import tr.depo.backend.service.SilmeService;
import tr.depo.backend.util.AktifKayitUtil;
import tr.depo.domain.ortak.BaseEntity;

/**
 * Bir aggregate'in alt kayıtları (envanterin barkodu, arızanın işlemi, sayımın satırı…) için ortak taban. Alt kayıtlar
 * yalnızca üst kayıt üzerinden erişilir ({@code /ust/{ustId}/alt/{id}}): üst kayıt yoksa ya da silinmişse, ya da alt
 * kayıt başka bir üst kayda aitse 404.
 *
 * @param <T> alt kayıt
 * @param <U> üst kayıt (aggregate root)
 */
public abstract class AbstractAltKayitServiceImpl<T extends BaseEntity, U extends BaseEntity> extends AbstractSoftDeleteServiceImpl<T> {

    private final AktifKayitRepository<U> ustRepository;
    private final String ustKaynakAdi;

    protected AbstractAltKayitServiceImpl(AktifKayitRepository<T> repository, AktifKayitRepository<U> ustRepository,
                                    SilmeService silmeService, String kaynakAdi, String ustKaynakAdi) {
        super(repository, silmeService, kaynakAdi);
        this.ustRepository = Objects.requireNonNull(ustRepository, "ustRepository");
        this.ustKaynakAdi = Objects.requireNonNull(ustKaynakAdi, "ustKaynakAdi");
    }

    /** Alt kaydın bağlı olduğu üst kayıt (legacy'de boş olabilir). */
    protected abstract U ustKaydi(T altKayit);

    /** Soft delete; alt kaydın bu üst kayda ait olduğu doğrulanır. */
    @Transactional
    public void sil(Long ustId, Long id) {
        aktifAltKayit(ustId, id);
        sil(id);
    }

    /** @throws KayitBulunamadiException üst kayıt yoksa ya da silinmişse (404) */
    protected U aktifUst(Long ustId) {
        return AktifKayitUtil.bul(ustRepository, ustId, ustKaynakAdi);
    }

    /** @throws KayitBulunamadiException üst ya da alt kayıt yoksa, silinmişse ya da alt kayıt başka üst kaydınsa */
    protected T aktifAltKayit(Long ustId, Long id) {
        aktifUst(ustId);
        T altKayit = aktifBul(id);
        U sahibi = ustKaydi(altKayit);
        if (sahibi == null || !ustId.equals(sahibi.getId())) {
            throw new KayitBulunamadiException(kaynakAdi(), id);
        }
        return altKayit;
    }
}
