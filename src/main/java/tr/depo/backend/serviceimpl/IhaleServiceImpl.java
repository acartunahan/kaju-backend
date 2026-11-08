package tr.depo.backend.serviceimpl;

import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.depo.backend.dto.CreateIhaleRequest;
import tr.depo.backend.dto.IhaleResponse;
import tr.depo.backend.dto.KullanimReferansi;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.UpdateIhaleRequest;
import tr.depo.backend.mapper.IhaleMapper;
import tr.depo.backend.repository.IhaleRepository;
import tr.depo.backend.service.IhaleService;
import tr.depo.backend.service.ReferansService;
import tr.depo.backend.service.SilmeService;
import tr.depo.backend.util.SiralamaDogrulayici;
import tr.depo.domain.envanter.Envanter;
import tr.depo.domain.tanim.Firma;
import tr.depo.domain.tanim.Ihale;
import tr.depo.domain.tanim.MalzemeTuru;

@Service
@Transactional(readOnly = true)
public class IhaleServiceImpl extends AbstractSoftDeleteServiceImpl<Ihale> implements IhaleService {

    static final String KAYNAK = "İhale";
    static final String FIRMA_ALANI = "firmaId";
    static final Set<String> SIRALAMA_ALANLARI = Set.of("id", "ad", "ihaleTarihi", "kayitTarihi", "olusturmaZamani");

    private static final List<KullanimReferansi> KULLANIM_REFERANSLARI = List.of(
        new KullanimReferansi(Envanter.class, "ihale", "envanter"));

    private final IhaleRepository repository;
    private final IhaleMapper mapper;
    private final ReferansService referansService;

    public IhaleServiceImpl(IhaleRepository repository, IhaleMapper mapper, ReferansService referansService,
                            SilmeService silmeService) {
        super(repository, silmeService, KAYNAK);
        this.repository = repository;
        this.mapper = mapper;
        this.referansService = referansService;
    }

    @Override
    public IhaleResponse getir(Long id) {
        return mapper.cevap(aktifBul(id));
    }

    @Override
    public SayfaResponse<IhaleResponse> listele(Pageable sayfa) {
        SiralamaDogrulayici.dogrula(sayfa, SIRALAMA_ALANLARI);
        return SayfaResponse.of(repository.findAll(sayfa).map(mapper::cevap));
    }

    @Override
    @Transactional
    public IhaleResponse olustur(CreateIhaleRequest istek) {
        Ihale ihale = mapper.yeni(istek);
        ihale.setFirma(demirbasFirmasi(istek.firmaId()));
        Ihale kaydedilen = repository.save(ihale);
        repository.flush();
        return mapper.cevap(kaydedilen);
    }

    @Override
    @Transactional
    public IhaleResponse guncelle(Long id, UpdateIhaleRequest istek) {
        Ihale ihale = aktifBul(id);
        surumuDogrula(ihale, istek.versiyon());
        Firma firma = demirbasFirmasi(istek.firmaId());
        mapper.guncelle(istek, ihale);
        ihale.setFirma(firma);
        repository.flush();
        return mapper.cevap(ihale);
    }

    @Override
    protected List<KullanimReferansi> kullanimReferanslari() {
        return KULLANIM_REFERANSLARI;
    }

    /** İhale yalnızca demirbaş firmasına bağlanır (domain ve FK_IHALE_FIRMA). */
    private Firma demirbasFirmasi(Long firmaId) {
        return referansService.aktifVeAyniTurde(Firma.class, firmaId, FIRMA_ALANI, MalzemeTuru.DEMIRBAS, Firma::getTur);
    }
}
