package tr.depo.backend.serviceimpl;

import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.depo.backend.dto.CreateEnvanterNotuRequest;
import tr.depo.backend.dto.EnvanterNotuResponse;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.UpdateEnvanterNotuRequest;
import tr.depo.backend.mapper.EnvanterNotuMapper;
import tr.depo.backend.repository.EnvanterNotuRepository;
import tr.depo.backend.repository.EnvanterRepository;
import tr.depo.backend.service.EnvanterNotuService;
import tr.depo.backend.service.SilmeService;
import tr.depo.backend.util.SiralamaDogrulayici;
import tr.depo.domain.envanter.Envanter;
import tr.depo.domain.envanter.EnvanterNotu;

@Service
@Transactional(readOnly = true)
public class EnvanterNotuServiceImpl extends AbstractAltKayitServiceImpl<EnvanterNotu, Envanter>
    implements EnvanterNotuService {

    static final String KAYNAK = "Not";
    static final Set<String> SIRALAMA_ALANLARI = Set.of("id", "kayitTarihi", "olusturmaZamani");

    private final EnvanterNotuRepository repository;
    private final EnvanterNotuMapper mapper;

    public EnvanterNotuServiceImpl(EnvanterNotuRepository repository, EnvanterNotuMapper mapper,
                                   EnvanterRepository envanterRepository, SilmeService silmeService) {
        super(repository, envanterRepository, silmeService, KAYNAK, EnvanterServiceImpl.KAYNAK);
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public EnvanterNotuResponse getir(Long envanterId, Long id) {
        return mapper.cevap(aktifAltKayit(envanterId, id));
    }

    @Override
    public SayfaResponse<EnvanterNotuResponse> listele(Long envanterId, Pageable sayfa) {
        SiralamaDogrulayici.dogrula(sayfa, SIRALAMA_ALANLARI);
        aktifUst(envanterId);
        return SayfaResponse.of(repository.findByEnvanter_Id(envanterId, sayfa).map(mapper::cevap));
    }

    @Override
    @Transactional
    public EnvanterNotuResponse olustur(Long envanterId, CreateEnvanterNotuRequest istek) {
        EnvanterNotu not = mapper.yeni(istek);
        not.setEnvanter(aktifUst(envanterId));
        EnvanterNotu kaydedilen = repository.save(not);
        repository.flush();
        return mapper.cevap(kaydedilen);
    }

    @Override
    @Transactional
    public EnvanterNotuResponse guncelle(Long envanterId, Long id, UpdateEnvanterNotuRequest istek) {
        EnvanterNotu not = aktifAltKayit(envanterId, id);
        surumuDogrula(not, istek.versiyon());
        mapper.guncelle(istek, not);
        repository.flush();
        return mapper.cevap(not);
    }

    @Override
    protected Envanter ustKaydi(EnvanterNotu not) {
        return not.getEnvanter();
    }
}
