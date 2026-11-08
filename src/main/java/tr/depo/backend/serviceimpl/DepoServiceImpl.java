package tr.depo.backend.serviceimpl;

import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.depo.backend.dto.CreateDepoRequest;
import tr.depo.backend.dto.DepoResponse;
import tr.depo.backend.dto.KullanimReferansi;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.UpdateDepoRequest;
import tr.depo.backend.mapper.DepoMapper;
import tr.depo.backend.repository.DepoRepository;
import tr.depo.backend.service.DepoService;
import tr.depo.backend.service.SilmeService;
import tr.depo.backend.util.SiralamaDogrulayici;
import tr.depo.domain.ariza.Ariza;
import tr.depo.domain.envanter.Envanter;
import tr.depo.domain.envanter.EnvanterHareketi;
import tr.depo.domain.envanter.ZimmetKaydi;
import tr.depo.domain.sayim.Sayim;
import tr.depo.domain.sayim.SayimSatiri;
import tr.depo.domain.tanim.Depo;
import tr.depo.domain.tanim.Firma;
import tr.depo.domain.tanim.Kategori;
import tr.depo.domain.tanim.Marka;
import tr.depo.domain.tanim.Model;

@Service
@Transactional(readOnly = true)
public class DepoServiceImpl extends AbstractSoftDeleteServiceImpl<Depo> implements DepoService {

    static final String KAYNAK = "Depo";
    static final Set<String> SIRALAMA_ALANLARI = Set.of("id", "ad", "kayitTarihi", "olusturmaZamani");

    /** Depoya referans veren tüm ilişkiler (domain şemasındaki DEPO FK'leri). */
    private static final List<KullanimReferansi> KULLANIM_REFERANSLARI = List.of(
        new KullanimReferansi(Envanter.class, "depo", "envanter"),
        new KullanimReferansi(EnvanterHareketi.class, "neredenDepo", "hareket (çıkış deposu)"),
        new KullanimReferansi(EnvanterHareketi.class, "nereyeDepo", "hareket (giriş deposu)"),
        new KullanimReferansi(EnvanterHareketi.class, "alanDepo", "sarf hareketi"),
        new KullanimReferansi(Ariza.class, "depo", "arıza"),
        new KullanimReferansi(Sayim.class, "depo", "sayım"),
        new KullanimReferansi(SayimSatiri.class, "depo", "sayım satırı"),
        new KullanimReferansi(ZimmetKaydi.class, "depo", "zimmet kaydı"),
        new KullanimReferansi(Marka.class, "depo", "marka"),
        new KullanimReferansi(Model.class, "depo", "model"),
        new KullanimReferansi(Kategori.class, "depo", "kategori"),
        new KullanimReferansi(Firma.class, "depo", "firma"));

    private final DepoRepository repository;
    private final DepoMapper mapper;

    public DepoServiceImpl(DepoRepository repository, DepoMapper mapper, SilmeService silmeService) {
        super(repository, silmeService, KAYNAK);
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public DepoResponse getir(Long id) {
        return mapper.cevap(aktifBul(id));
    }

    @Override
    public SayfaResponse<DepoResponse> listele(Pageable sayfa) {
        SiralamaDogrulayici.dogrula(sayfa, SIRALAMA_ALANLARI);
        return SayfaResponse.of(repository.findAll(sayfa).map(mapper::cevap));
    }

    @Override
    @Transactional
    public DepoResponse olustur(CreateDepoRequest istek) {
        Depo kaydedilen = repository.save(mapper.yeni(istek));
        repository.flush();
        return mapper.cevap(kaydedilen);
    }

    @Override
    @Transactional
    public DepoResponse guncelle(Long id, UpdateDepoRequest istek) {
        Depo depo = aktifBul(id);
        surumuDogrula(depo, istek.versiyon());
        mapper.guncelle(istek, depo);
        repository.flush();
        return mapper.cevap(depo);
    }

    @Override
    protected List<KullanimReferansi> kullanimReferanslari() {
        return KULLANIM_REFERANSLARI;
    }
}
