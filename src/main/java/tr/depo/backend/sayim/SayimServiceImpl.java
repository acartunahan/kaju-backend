package tr.depo.backend.sayim;

import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.depo.backend.common.paging.SayfaCevabi;
import tr.depo.backend.common.paging.SiralamaDogrulayici;
import tr.depo.backend.common.persistence.ReferansYukleyici;
import tr.depo.backend.common.service.SilmeAltyapisi;
import tr.depo.backend.common.service.SoftDeleteServisSablonu;
import tr.depo.backend.sayim.dto.CreateSayimRequest;
import tr.depo.backend.sayim.dto.SayimResponse;
import tr.depo.backend.sayim.dto.UpdateSayimRequest;
import tr.depo.domain.sayim.Sayim;
import tr.depo.domain.sayim.SayimSatiri;
import tr.depo.domain.tanim.Depo;

@Service
@Transactional(readOnly = true)
public class SayimServiceImpl extends SoftDeleteServisSablonu<Sayim> implements SayimService {

    static final String KAYNAK = "Sayım";
    static final String DEPO_ALANI = "depoId";
    static final Set<String> SIRALAMA_ALANLARI = Set.of("id", "baslik", "baslangicTarihi", "olusturmaZamani");

    private final SayimRepository repository;
    private final SayimMapper mapper;
    private final ReferansYukleyici referansYukleyici;
    private final SilmeAltyapisi silmeAltyapisi;

    public SayimServiceImpl(SayimRepository repository, SayimMapper mapper, ReferansYukleyici referansYukleyici,
                            SilmeAltyapisi silmeAltyapisi) {
        super(repository, silmeAltyapisi, KAYNAK);
        this.repository = repository;
        this.mapper = mapper;
        this.referansYukleyici = referansYukleyici;
        this.silmeAltyapisi = silmeAltyapisi;
    }

    @Override
    public SayimResponse getir(Long id) {
        return mapper.cevap(aktifBul(id));
    }

    @Override
    public SayfaCevabi<SayimResponse> listele(Pageable sayfa) {
        SiralamaDogrulayici.dogrula(sayfa, SIRALAMA_ALANLARI);
        return SayfaCevabi.of(repository.findAll(sayfa).map(mapper::cevap));
    }

    @Override
    @Transactional
    public SayimResponse olustur(CreateSayimRequest istek) {
        Sayim sayim = mapper.yeni(istek);
        sayim.setDepo(referansYukleyici.aktif(Depo.class, istek.depoId(), DEPO_ALANI));
        Sayim kaydedilen = repository.save(sayim);
        repository.flush();
        return mapper.cevap(kaydedilen);
    }

    @Override
    @Transactional
    public SayimResponse guncelle(Long id, UpdateSayimRequest istek) {
        Sayim sayim = aktifBul(id);
        surumuDogrula(sayim, istek.versiyon());
        mapper.guncelle(istek, sayim);
        sayim.setDepo(referansYukleyici.aktif(Depo.class, istek.depoId(), DEPO_ALANI));
        repository.flush();
        return mapper.cevap(sayim);
    }

    @Override
    protected void altKayitlariSil(Sayim sayim, String silen) {
        silmeAltyapisi.aktifAltKayitlariSil(SayimSatiri.class, "sayim", sayim, silen);
    }
}
