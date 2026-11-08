package tr.depo.backend.serviceimpl;

import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.depo.backend.dto.BarkodResponse;
import tr.depo.backend.dto.CreateBarkodRequest;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.UpdateBarkodRequest;
import tr.depo.backend.mapper.BarkodMapper;
import tr.depo.backend.repository.BarkodRepository;
import tr.depo.backend.repository.EnvanterRepository;
import tr.depo.backend.service.BarkodService;
import tr.depo.backend.service.SilmeService;
import tr.depo.backend.util.SiralamaDogrulayici;
import tr.depo.domain.envanter.Barkod;
import tr.depo.domain.envanter.Envanter;

@Service
@Transactional(readOnly = true)
public class BarkodServiceImpl extends AbstractAltKayitServiceImpl<Barkod, Envanter> implements BarkodService {

    static final String KAYNAK = "Barkod";
    static final Set<String> SIRALAMA_ALANLARI = Set.of("id", "barkod", "olusturmaZamani");

    private final BarkodRepository repository;
    private final BarkodMapper mapper;

    public BarkodServiceImpl(BarkodRepository repository, BarkodMapper mapper, EnvanterRepository envanterRepository,
                             SilmeService silmeService) {
        super(repository, envanterRepository, silmeService, KAYNAK, EnvanterServiceImpl.KAYNAK);
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public BarkodResponse getir(Long envanterId, Long id) {
        return mapper.cevap(aktifAltKayit(envanterId, id));
    }

    @Override
    public SayfaResponse<BarkodResponse> listele(Long envanterId, Pageable sayfa) {
        SiralamaDogrulayici.dogrula(sayfa, SIRALAMA_ALANLARI);
        aktifUst(envanterId);
        return SayfaResponse.of(repository.findByEnvanter_Id(envanterId, sayfa).map(mapper::cevap));
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
