package tr.depo.backend.envanter.sarf;

import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.depo.backend.common.exception.KayitBulunamadiException;
import tr.depo.backend.common.paging.SayfaCevabi;
import tr.depo.backend.common.paging.SiralamaDogrulayici;
import tr.depo.backend.common.service.SilmeAltyapisi;
import tr.depo.backend.common.service.SoftDeleteServisSablonu;
import tr.depo.backend.envanter.EnvanterAltKayitlari;
import tr.depo.backend.envanter.EnvanterIliskileri;
import tr.depo.backend.envanter.EnvanterRepository;
import tr.depo.backend.envanter.sarf.dto.CreateSarfMalzemesiRequest;
import tr.depo.backend.envanter.sarf.dto.SarfMalzemesiResponse;
import tr.depo.backend.envanter.sarf.dto.UpdateSarfMalzemesiRequest;
import tr.depo.domain.envanter.Envanter;
import tr.depo.domain.tanim.MalzemeTuru;

@Service
@Transactional(readOnly = true)
public class SarfMalzemesiServiceImpl extends SoftDeleteServisSablonu<Envanter> implements SarfMalzemesiService {

    static final String KAYNAK = "Sarf malzemesi";
    static final Set<String> SIRALAMA_ALANLARI = Set.of("id", "ad", "kayitTarihi", "olusturmaZamani");

    private final EnvanterRepository repository;
    private final SarfMalzemesiMapper mapper;
    private final EnvanterIliskileri iliskiler;
    private final EnvanterAltKayitlari altKayitlar;

    public SarfMalzemesiServiceImpl(EnvanterRepository repository, SarfMalzemesiMapper mapper,
                                    EnvanterIliskileri iliskiler, EnvanterAltKayitlari altKayitlar,
                                    SilmeAltyapisi silmeAltyapisi) {
        super(repository, silmeAltyapisi, KAYNAK);
        this.repository = repository;
        this.mapper = mapper;
        this.iliskiler = iliskiler;
        this.altKayitlar = altKayitlar;
    }

    @Override
    public SarfMalzemesiResponse getir(Long id) {
        return mapper.cevap(aktifBul(id));
    }

    @Override
    public SayfaCevabi<SarfMalzemesiResponse> listele(Pageable sayfa) {
        SiralamaDogrulayici.dogrula(sayfa, SIRALAMA_ALANLARI);
        return SayfaCevabi.of(repository.findByTur(MalzemeTuru.SARF, sayfa).map(mapper::cevap));
    }

    @Override
    @Transactional
    public SarfMalzemesiResponse olustur(CreateSarfMalzemesiRequest istek) {
        Envanter sarf = mapper.yeni(istek);
        iliskiler.bagla(sarf, istek.kategoriId(), istek.markaId(), istek.modelId(), istek.depoId(), null);
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
        iliskiler.bagla(sarf, istek.kategoriId(), istek.markaId(), istek.modelId(), istek.depoId(), null);
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
        altKayitlar.sil(envanter, silen);
    }
}
