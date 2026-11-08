package tr.depo.backend.serviceimpl;

import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.depo.backend.dto.CreateSarfMalzemesiRequest;
import tr.depo.backend.dto.SarfMalzemesiResponse;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.UpdateSarfMalzemesiRequest;
import tr.depo.backend.exception.KayitBulunamadiException;
import tr.depo.backend.mapper.SarfMalzemesiMapper;
import tr.depo.backend.repository.EnvanterRepository;
import tr.depo.backend.service.EnvanterService;
import tr.depo.backend.service.SarfMalzemesiService;
import tr.depo.backend.service.SilmeService;
import tr.depo.backend.util.SiralamaDogrulayici;
import tr.depo.domain.envanter.Envanter;
import tr.depo.domain.tanim.MalzemeTuru;

@Service
@Transactional(readOnly = true)
public class SarfMalzemesiServiceImpl extends AbstractSoftDeleteServiceImpl<Envanter> implements SarfMalzemesiService {

    static final String KAYNAK = "Sarf malzemesi";
    static final Set<String> SIRALAMA_ALANLARI = Set.of("id", "ad", "kayitTarihi", "olusturmaZamani");

    private final EnvanterRepository repository;
    private final SarfMalzemesiMapper mapper;
    private final EnvanterService envanterService;

    public SarfMalzemesiServiceImpl(EnvanterRepository repository, SarfMalzemesiMapper mapper,
                                    EnvanterService envanterService, SilmeService silmeService) {
        super(repository, silmeService, KAYNAK);
        this.repository = repository;
        this.mapper = mapper;
        this.envanterService = envanterService;
    }

    @Override
    public SarfMalzemesiResponse getir(Long id) {
        return mapper.cevap(aktifBul(id));
    }

    @Override
    public SayfaResponse<SarfMalzemesiResponse> listele(Pageable sayfa) {
        SiralamaDogrulayici.dogrula(sayfa, SIRALAMA_ALANLARI);
        return SayfaResponse.of(repository.findByTur(MalzemeTuru.SARF, sayfa).map(mapper::cevap));
    }

    @Override
    @Transactional
    public SarfMalzemesiResponse olustur(CreateSarfMalzemesiRequest istek) {
        Envanter sarf = mapper.yeni(istek);
        envanterService.bagla(sarf, istek.kategoriId(), istek.markaId(), istek.modelId(), istek.depoId(), null);
        Envanter kaydedilen = repository.save(sarf);
        repository.flush();
        return mapper.cevap(kaydedilen);
    }

    @Override
    @Transactional
    public SarfMalzemesiResponse guncelle(Long id, UpdateSarfMalzemesiRequest istek) {
        Envanter sarf = aktifBul(id);
        surumuDogrula(sarf, istek.versiyon());
        mapper.guncelle(istek, sarf);
        envanterService.bagla(sarf, istek.kategoriId(), istek.markaId(), istek.modelId(), istek.depoId(), null);
        repository.flush();
        return mapper.cevap(sarf);
    }

    /** Aynı tabloyu demirbaşlarla paylaşır: bu kaynaktan yalnızca sarf malzemeleri görünür. */
    @Override
    protected Envanter aktifBul(Long id) {
        Envanter envanter = super.aktifBul(id);
        if (envanter.demirbasMi()) {
            throw new KayitBulunamadiException(KAYNAK, id);
        }
        return envanter;
    }

    @Override
    protected void altKayitlariSil(Envanter envanter, String silen) {
        envanterService.altKayitlariSil(envanter, silen);
    }
}
