package tr.depo.backend.ariza.islem;

import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.depo.backend.ariza.ArizaRepository;
import tr.depo.backend.ariza.islem.dto.ArizaIslemiResponse;
import tr.depo.backend.ariza.islem.dto.CreateArizaIslemiRequest;
import tr.depo.backend.ariza.islem.dto.UpdateArizaIslemiRequest;
import tr.depo.backend.common.paging.SayfaCevabi;
import tr.depo.backend.common.paging.SiralamaDogrulayici;
import tr.depo.backend.common.service.AltKayitServisSablonu;
import tr.depo.backend.common.service.SilmeAltyapisi;
import tr.depo.domain.ariza.Ariza;
import tr.depo.domain.ariza.ArizaIslemi;

@Service
@Transactional(readOnly = true)
public class ArizaIslemiServiceImpl extends AltKayitServisSablonu<ArizaIslemi, Ariza> implements ArizaIslemiService {

    static final String KAYNAK = "Arıza işlemi";
    static final String UST_KAYNAK = "Arıza";
    static final Set<String> SIRALAMA_ALANLARI = Set.of("id", "zaman", "siraNo", "olusturmaZamani");

    private final ArizaIslemiRepository repository;
    private final ArizaIslemiMapper mapper;

    public ArizaIslemiServiceImpl(ArizaIslemiRepository repository, ArizaIslemiMapper mapper,
                                  ArizaRepository arizaRepository, SilmeAltyapisi silmeAltyapisi) {
        super(repository, arizaRepository, silmeAltyapisi, KAYNAK, UST_KAYNAK);
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public ArizaIslemiResponse getir(Long arizaId, Long id) {
        return mapper.cevap(aktifAltKayit(arizaId, id));
    }

    @Override
    public SayfaCevabi<ArizaIslemiResponse> listele(Long arizaId, Pageable sayfa) {
        SiralamaDogrulayici.dogrula(sayfa, SIRALAMA_ALANLARI);
        aktifUst(arizaId);
        return SayfaCevabi.of(repository.findByAriza_Id(arizaId, sayfa).map(mapper::cevap));
    }

    @Override
    @Transactional
    public ArizaIslemiResponse olustur(Long arizaId, CreateArizaIslemiRequest istek) {
        ArizaIslemi islem = mapper.yeni(istek);
        islem.setAriza(aktifUst(arizaId));
        ArizaIslemi kaydedilen = repository.save(islem);
        repository.flush();
        return mapper.cevap(kaydedilen);
    }

    @Override
    @Transactional
    public ArizaIslemiResponse guncelle(Long arizaId, Long id, UpdateArizaIslemiRequest istek) {
        ArizaIslemi islem = aktifAltKayit(arizaId, id);
        surumuDogrula(islem, istek.versiyon());
        mapper.guncelle(istek, islem);
        repository.flush();
        return mapper.cevap(islem);
    }

    @Override
    protected Ariza ustKaydi(ArizaIslemi islem) {
        return islem.getAriza();
    }
}
