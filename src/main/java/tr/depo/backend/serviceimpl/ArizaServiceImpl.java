package tr.depo.backend.serviceimpl;

import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.depo.backend.dto.ArizaResponse;
import tr.depo.backend.dto.CreateArizaRequest;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.UpdateArizaRequest;
import tr.depo.backend.mapper.ArizaMapper;
import tr.depo.backend.repository.ArizaRepository;
import tr.depo.backend.service.ArizaService;
import tr.depo.backend.service.EnvanterService;
import tr.depo.backend.service.ReferansService;
import tr.depo.backend.service.SilmeService;
import tr.depo.backend.util.SiralamaDogrulayici;
import tr.depo.domain.ariza.Ariza;
import tr.depo.domain.ariza.ArizaIslemi;
import tr.depo.domain.envanter.Envanter;
import tr.depo.domain.tanim.Depo;
import tr.depo.domain.tanim.MalzemeTuru;

@Service
@Transactional(readOnly = true)
public class ArizaServiceImpl extends AbstractSoftDeleteServiceImpl<Ariza> implements ArizaService {

    static final String KAYNAK = "Arıza";
    static final Set<String> SIRALAMA_ALANLARI = Set.of("id", "tarih", "miad", "olusturmaZamani");
    private static final Sort EN_YENI_ONCE = Sort.by(Sort.Direction.DESC, "tarih", "id");

    private final ArizaRepository repository;
    private final ArizaMapper mapper;
    private final ReferansService referansService;
    private final SilmeService silmeService;
    private final EnvanterService envanterService;

    public ArizaServiceImpl(ArizaRepository repository, ArizaMapper mapper, ReferansService referansService,
                            SilmeService silmeService, EnvanterService envanterService) {
        super(repository, silmeService, KAYNAK);
        this.repository = repository;
        this.mapper = mapper;
        this.referansService = referansService;
        this.silmeService = silmeService;
        this.envanterService = envanterService;
    }

    @Override
    public ArizaResponse getir(Long id) {
        return mapper.cevap(aktifBul(id));
    }

    @Override
    public SayfaResponse<ArizaResponse> listele(Pageable sayfa) {
        SiralamaDogrulayici.dogrula(sayfa, SIRALAMA_ALANLARI);
        return SayfaResponse.of(repository.findAll(sayfa).map(mapper::cevap));
    }

    @Override
    public SayfaResponse<ArizaResponse> demirbasArizalari(Long demirbasId, Pageable sayfa) {
        envanterService.aktifBul(demirbasId, MalzemeTuru.DEMIRBAS, "Demirbaş");
        SiralamaDogrulayici.dogrula(sayfa, SIRALAMA_ALANLARI);
        return SayfaResponse.of(repository.findByEnvanter_Id(demirbasId,
            SiralamaDogrulayici.varsayilanSiralama(sayfa, EN_YENI_ONCE)).map(mapper::cevap));
    }

    @Override
    @Transactional
    public ArizaResponse olustur(CreateArizaRequest istek) {
        Ariza ariza = mapper.yeni(istek);
        iliskileriBagla(ariza, istek.envanterId(), istek.depoId());
        Ariza kaydedilen = repository.save(ariza);
        repository.flush();
        return mapper.cevap(kaydedilen);
    }

    @Override
    @Transactional
    public ArizaResponse guncelle(Long id, UpdateArizaRequest istek) {
        Ariza ariza = aktifBul(id);
        surumuDogrula(ariza, istek.versiyon());
        mapper.guncelle(istek, ariza);
        iliskileriBagla(ariza, istek.envanterId(), istek.depoId());
        repository.flush();
        return mapper.cevap(ariza);
    }

    @Override
    protected void altKayitlariSil(Ariza ariza, String silen) {
        silmeService.aktifAltKayitlariSil(ArizaIslemi.class, "ariza", ariza, silen);
    }

    private void iliskileriBagla(Ariza ariza, Long envanterId, Long depoId) {
        Envanter envanter = referansService.aktif(Envanter.class, envanterId, "envanterId");
        Depo depo = referansService.aktif(Depo.class, depoId, "depoId");
        ariza.setEnvanter(envanter);
        ariza.setDepo(depo);
    }
}
