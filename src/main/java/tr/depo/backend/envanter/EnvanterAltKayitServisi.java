package tr.depo.backend.envanter;

import tr.depo.backend.common.persistence.AktifKayitRepository;
import tr.depo.backend.common.service.AltKayitServisSablonu;
import tr.depo.backend.common.service.SilmeAltyapisi;
import tr.depo.domain.envanter.Envanter;
import tr.depo.domain.ortak.BaseEntity;

/** Envanter aggregate'inin alt kayıtları (barkod, not, aparat) için taban: {@code /envanterler/{envanterId}/...}. */
public abstract class EnvanterAltKayitServisi<T extends BaseEntity> extends AltKayitServisSablonu<T, Envanter> {

    static final String ENVANTER_KAYNAGI = "Envanter";

    protected EnvanterAltKayitServisi(AktifKayitRepository<T> repository, EnvanterRepository envanterRepository,
                                      SilmeAltyapisi silmeAltyapisi, String kaynakAdi) {
        super(repository, envanterRepository, silmeAltyapisi, kaynakAdi, ENVANTER_KAYNAGI);
    }
}
