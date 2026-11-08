package tr.depo.backend.serviceimpl;

import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.depo.backend.dto.CreateSayimRequest;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.SayimResponse;
import tr.depo.backend.dto.UpdateSayimRequest;
import tr.depo.backend.mapper.SayimMapper;
import tr.depo.backend.repository.SayimRepository;
import tr.depo.backend.service.ReferansService;
import tr.depo.backend.service.SayimService;
import tr.depo.backend.service.SilmeService;
import tr.depo.backend.util.SiralamaDogrulayici;
import tr.depo.domain.sayim.Sayim;
import tr.depo.domain.sayim.SayimSatiri;
import tr.depo.domain.tanim.Depo;

@Service
@Transactional(readOnly = true)
public class SayimServiceImpl extends AbstractSoftDeleteServiceImpl<Sayim> implements SayimService {

    static final String KAYNAK = "Sayım";
    static final String DEPO_ALANI = "depoId";
    static final Set<String> SIRALAMA_ALANLARI = Set.of("id", "baslik", "baslangicTarihi", "olusturmaZamani");

    private final SayimRepository repository;
    private final SayimMapper mapper;
    private final ReferansService referansService;
    private final SilmeService silmeService;

    public SayimServiceImpl(SayimRepository repository, SayimMapper mapper, ReferansService referansService,
                            SilmeService silmeService) {
        super(repository, silmeService, KAYNAK);
        this.repository = repository;
        this.mapper = mapper;
        this.referansService = referansService;
        this.silmeService = silmeService;
    }

    @Override
    public SayimResponse getir(Long id) {
        return mapper.cevap(aktifBul(id));
    }

    @Override
    public SayfaResponse<SayimResponse> listele(Pageable sayfa) {
        SiralamaDogrulayici.dogrula(sayfa, SIRALAMA_ALANLARI);
        return SayfaResponse.of(repository.findAll(sayfa).map(mapper::cevap));
    }

    @Override
    @Transactional
    public SayimResponse olustur(CreateSayimRequest istek) {
        Sayim sayim = mapper.yeni(istek);
        sayim.setDepo(referansService.aktif(Depo.class, istek.depoId(), DEPO_ALANI));
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
        sayim.setDepo(referansService.aktif(Depo.class, istek.depoId(), DEPO_ALANI));
        repository.flush();
        return mapper.cevap(sayim);
    }

    @Override
    protected void altKayitlariSil(Sayim sayim, String silen) {
        silmeService.aktifAltKayitlariSil(SayimSatiri.class, "sayim", sayim, silen);
    }
}
