package tr.depo.backend.ihale;

import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.depo.backend.common.paging.SayfaCevabi;
import tr.depo.backend.common.paging.SiralamaDogrulayici;
import tr.depo.backend.common.persistence.ReferansYukleyici;
import tr.depo.backend.common.service.KullanimReferansi;
import tr.depo.backend.common.service.SilmeAltyapisi;
import tr.depo.backend.common.service.SoftDeleteServisSablonu;
import tr.depo.backend.ihale.dto.CreateIhaleRequest;
import tr.depo.backend.ihale.dto.IhaleResponse;
import tr.depo.backend.ihale.dto.UpdateIhaleRequest;
import tr.depo.domain.envanter.Envanter;
import tr.depo.domain.tanim.Firma;
import tr.depo.domain.tanim.Ihale;
import tr.depo.domain.tanim.MalzemeTuru;

@Service
@Transactional(readOnly = true)
public class IhaleServiceImpl extends SoftDeleteServisSablonu<Ihale> implements IhaleService {

    static final String KAYNAK = "İhale";
    static final String FIRMA_ALANI = "firmaId";
    static final Set<String> SIRALAMA_ALANLARI = Set.of("id", "ad", "ihaleTarihi", "kayitTarihi", "olusturmaZamani");

    private static final List<KullanimReferansi> KULLANIM_REFERANSLARI = List.of(
        new KullanimReferansi(Envanter.class, "ihale", "envanter"));

    private final IhaleRepository repository;
    private final IhaleMapper mapper;
    private final ReferansYukleyici referansYukleyici;

    public IhaleServiceImpl(IhaleRepository repository, IhaleMapper mapper, ReferansYukleyici referansYukleyici,
                            SilmeAltyapisi silmeAltyapisi) {
        super(repository, silmeAltyapisi, KAYNAK);
        this.repository = repository;
        this.mapper = mapper;
        this.referansYukleyici = referansYukleyici;
    }

    @Override
    public IhaleResponse getir(Long id) {
        return mapper.cevap(aktifBul(id));
    }

    @Override
    public SayfaCevabi<IhaleResponse> listele(Pageable sayfa) {
        SiralamaDogrulayici.dogrula(sayfa, SIRALAMA_ALANLARI);
        return SayfaCevabi.of(repository.findAll(sayfa).map(mapper::cevap));
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
        return referansYukleyici.aktifVeAyniTurde(Firma.class, firmaId, FIRMA_ALANI, MalzemeTuru.DEMIRBAS, Firma::getTur);
    }
}
