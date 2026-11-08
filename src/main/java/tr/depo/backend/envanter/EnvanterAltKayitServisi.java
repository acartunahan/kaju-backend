package tr.depo.backend.envanter;

import java.util.Objects;
import org.springframework.transaction.annotation.Transactional;
import tr.depo.backend.common.exception.KayitBulunamadiException;
import tr.depo.backend.common.persistence.AktifKayitRepository;
import tr.depo.backend.common.service.SilmeAltyapisi;
import tr.depo.backend.common.service.SoftDeleteServisSablonu;
import tr.depo.domain.envanter.Envanter;
import tr.depo.domain.ortak.BaseEntity;

/**
 * Envanter aggregate'inin alt kayıtları (barkod, not, aparat) için ortak taban. Alt kayıtlar yalnızca envanter
 * üzerinden erişilir ({@code /envanterler/{envanterId}/...}): envanter yoksa ya da silinmişse, ya da alt kayıt başka
 * bir envantere aitse 404.
 */
public abstract class EnvanterAltKayitServisi<T extends BaseEntity> extends SoftDeleteServisSablonu<T> {

    static final String ENVANTER_KAYNAGI = "Envanter";

    private final EnvanterRepository envanterRepository;

    protected EnvanterAltKayitServisi(AktifKayitRepository<T> repository, EnvanterRepository envanterRepository,
                                      SilmeAltyapisi silmeAltyapisi, String kaynakAdi) {
        super(repository, silmeAltyapisi, kaynakAdi);
        this.envanterRepository = Objects.requireNonNull(envanterRepository, "envanterRepository");
    }

    /** Alt kaydın bağlı olduğu envanter (legacy'de boş olabilir). */
    protected abstract Envanter envanteri(T altKayit);

    /** Soft delete; alt kaydın bu envantere ait olduğu doğrulanır. */
    @Transactional
    public void sil(Long envanterId, Long id) {
        aktifAltKayit(envanterId, id);
        sil(id);
    }

    /** @throws KayitBulunamadiException envanter yoksa ya da silinmişse (404) */
    protected Envanter aktifEnvanter(Long envanterId) {
        return envanterRepository.findById(envanterId)
            .filter(envanter -> !envanter.isSilindi())
            .orElseThrow(() -> new KayitBulunamadiException(ENVANTER_KAYNAGI, envanterId));
    }

    /** @throws KayitBulunamadiException envanter ya da alt kayıt yoksa, silinmişse ya da alt kayıt başka envanterinse */
    protected T aktifAltKayit(Long envanterId, Long id) {
        aktifEnvanter(envanterId);
        T altKayit = aktifBul(id);
        Envanter sahibi = envanteri(altKayit);
        if (sahibi == null || !envanterId.equals(sahibi.getId())) {
            throw new KayitBulunamadiException(kaynakAdi(), id);
        }
        return altKayit;
    }
}
