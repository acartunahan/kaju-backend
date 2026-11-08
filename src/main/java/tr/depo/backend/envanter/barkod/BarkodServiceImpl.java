package tr.depo.backend.envanter.barkod;

import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.depo.backend.common.paging.SayfaCevabi;
import tr.depo.backend.common.paging.SiralamaDogrulayici;
import tr.depo.backend.common.service.SilmeAltyapisi;
import tr.depo.backend.envanter.EnvanterAltKayitServisi;
import tr.depo.backend.envanter.EnvanterRepository;
import tr.depo.backend.envanter.barkod.dto.BarkodResponse;
import tr.depo.backend.envanter.barkod.dto.CreateBarkodRequest;
import tr.depo.backend.envanter.barkod.dto.UpdateBarkodRequest;
import tr.depo.domain.envanter.Barkod;
import tr.depo.domain.envanter.Envanter;

@Service
@Transactional(readOnly = true)
public class BarkodServiceImpl extends EnvanterAltKayitServisi<Barkod> implements BarkodService {

    static final String KAYNAK = "Barkod";
    static final Set<String> SIRALAMA_ALANLARI = Set.of("id", "barkod", "olusturmaZamani");

    private final BarkodRepository repository;
    private final BarkodMapper mapper;

    public BarkodServiceImpl(BarkodRepository repository, BarkodMapper mapper, EnvanterRepository envanterRepository,
                             SilmeAltyapisi silmeAltyapisi) {
        super(repository, envanterRepository, silmeAltyapisi, KAYNAK);
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public BarkodResponse getir(Long envanterId, Long id) {
        return mapper.cevap(aktifAltKayit(envanterId, id));
    }

    @Override
    public SayfaCevabi<BarkodResponse> listele(Long envanterId, Pageable sayfa) {
        SiralamaDogrulayici.dogrula(sayfa, SIRALAMA_ALANLARI);
        aktifUst(envanterId);
        return SayfaCevabi.of(repository.findByEnvanter_Id(envanterId, sayfa).map(mapper::cevap));
    }

    @Override
    @Transactional
    public BarkodResponse olustur(Long envanterId, CreateBarkodRequest istek) {
        Barkod barkod = mapper.yeni(istek);
        barkod.setEnvanter(aktifUst(envanterId));
        Barkod kaydedilen = repository.save(barkod);
        repository.flush();
        return mapper.cevap(kaydedilen);
    }

    @Override
    @Transactional
    public BarkodResponse guncelle(Long envanterId, Long id, UpdateBarkodRequest istek) {
        Barkod barkod = aktifAltKayit(envanterId, id);
        surumuDogrula(barkod, istek.versiyon());
        mapper.guncelle(istek, barkod);
        repository.flush();
        return mapper.cevap(barkod);
    }

    @Override
    protected Envanter ustKaydi(Barkod barkod) {
        return barkod.getEnvanter();
    }
}
