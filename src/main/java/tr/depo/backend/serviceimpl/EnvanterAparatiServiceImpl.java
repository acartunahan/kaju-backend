package tr.depo.backend.serviceimpl;

import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.depo.backend.dto.CreateEnvanterAparatiRequest;
import tr.depo.backend.dto.EnvanterAparatiResponse;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.UpdateEnvanterAparatiRequest;
import tr.depo.backend.mapper.EnvanterAparatiMapper;
import tr.depo.backend.repository.EnvanterAparatiRepository;
import tr.depo.backend.repository.EnvanterRepository;
import tr.depo.backend.service.EnvanterAparatiService;
import tr.depo.backend.service.SilmeService;
import tr.depo.backend.util.SiralamaDogrulayici;
import tr.depo.domain.envanter.Envanter;
import tr.depo.domain.envanter.EnvanterAparati;

@Service
@Transactional(readOnly = true)
public class EnvanterAparatiServiceImpl extends AbstractAltKayitServiceImpl<EnvanterAparati, Envanter>
    implements EnvanterAparatiService {

    static final String KAYNAK = "Aparat";
    static final Set<String> SIRALAMA_ALANLARI = Set.of("id", "ad", "olusturmaZamani");

    private final EnvanterAparatiRepository repository;
    private final EnvanterAparatiMapper mapper;

    public EnvanterAparatiServiceImpl(EnvanterAparatiRepository repository, EnvanterAparatiMapper mapper,
                                      EnvanterRepository envanterRepository, SilmeService silmeService) {
        super(repository, envanterRepository, silmeService, KAYNAK, EnvanterServiceImpl.KAYNAK);
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public EnvanterAparatiResponse getir(Long envanterId, Long id) {
        return mapper.cevap(aktifAltKayit(envanterId, id));
    }

    @Override
    public SayfaResponse<EnvanterAparatiResponse> listele(Long envanterId, Pageable sayfa) {
        SiralamaDogrulayici.dogrula(sayfa, SIRALAMA_ALANLARI);
        aktifUst(envanterId);
        return SayfaResponse.of(repository.findByEnvanter_Id(envanterId, sayfa).map(mapper::cevap));
    }

    @Override
    @Transactional
    public EnvanterAparatiResponse olustur(Long envanterId, CreateEnvanterAparatiRequest istek) {
        EnvanterAparati kaydedilen = repository.save(mapper.yeni(istek, aktifUst(envanterId)));
        repository.flush();
        return mapper.cevap(kaydedilen);
    }

    @Override
    @Transactional
    public EnvanterAparatiResponse guncelle(Long envanterId, Long id, UpdateEnvanterAparatiRequest istek) {
        EnvanterAparati aparat = aktifAltKayit(envanterId, id);
        surumuDogrula(aparat, istek.versiyon());
        mapper.guncelle(istek, aparat);
        repository.flush();
        return mapper.cevap(aparat);
    }

    @Override
    protected Envanter ustKaydi(EnvanterAparati aparat) {
        return aparat.getEnvanter();
    }
}
