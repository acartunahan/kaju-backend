package tr.depo.backend.ariza;

import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.depo.backend.ariza.dto.ArizaResponse;
import tr.depo.backend.ariza.dto.CreateArizaRequest;
import tr.depo.backend.ariza.dto.UpdateArizaRequest;
import tr.depo.backend.common.paging.SayfaCevabi;
import tr.depo.backend.common.paging.SiralamaDogrulayici;
import tr.depo.backend.common.persistence.ReferansYukleyici;
import tr.depo.backend.common.service.SilmeAltyapisi;
import tr.depo.backend.common.service.SoftDeleteServisSablonu;
import tr.depo.domain.ariza.Ariza;
import tr.depo.domain.ariza.ArizaIslemi;
import tr.depo.domain.envanter.Envanter;
import tr.depo.domain.tanim.Depo;

@Service
@Transactional(readOnly = true)
public class ArizaServiceImpl extends SoftDeleteServisSablonu<Ariza> implements ArizaService {

    static final String KAYNAK = "Arıza";
    static final Set<String> SIRALAMA_ALANLARI = Set.of("id", "tarih", "miad", "olusturmaZamani");

    private final ArizaRepository repository;
    private final ArizaMapper mapper;
    private final ReferansYukleyici referansYukleyici;
    private final SilmeAltyapisi silmeAltyapisi;

    public ArizaServiceImpl(ArizaRepository repository, ArizaMapper mapper, ReferansYukleyici referansYukleyici,
                            SilmeAltyapisi silmeAltyapisi) {
        super(repository, silmeAltyapisi, KAYNAK);
        this.repository = repository;
        this.mapper = mapper;
        this.referansYukleyici = referansYukleyici;
        this.silmeAltyapisi = silmeAltyapisi;
    }

    @Override
    public ArizaResponse getir(Long id) {
        return mapper.cevap(aktifBul(id));
    }

    @Override
    public SayfaCevabi<ArizaResponse> listele(Pageable sayfa) {
        SiralamaDogrulayici.dogrula(sayfa, SIRALAMA_ALANLARI);
        return SayfaCevabi.of(repository.findAll(sayfa).map(mapper::cevap));
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
        silmeAltyapisi.aktifAltKayitlariSil(ArizaIslemi.class, "ariza", ariza, silen);
    }

    private void iliskileriBagla(Ariza ariza, Long envanterId, Long depoId) {
        Envanter envanter = referansYukleyici.aktif(Envanter.class, envanterId, "envanterId");
        Depo depo = referansYukleyici.aktif(Depo.class, depoId, "depoId");
        ariza.setEnvanter(envanter);
        ariza.setDepo(depo);
    }
}
