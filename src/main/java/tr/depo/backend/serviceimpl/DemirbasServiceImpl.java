package tr.depo.backend.serviceimpl;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.depo.backend.dto.CreateDemirbasRequest;
import tr.depo.backend.dto.DemirbasFiltreRequest;
import tr.depo.backend.dto.DemirbasResponse;
import tr.depo.backend.dto.EnvanterAramaKriteri;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.UpdateDemirbasRequest;
import tr.depo.backend.exception.GecersizIstekException;
import tr.depo.backend.exception.HataKodu;
import tr.depo.backend.exception.KayitBulunamadiException;
import tr.depo.backend.exception.TekillikIhlaliException;
import tr.depo.backend.mapper.DemirbasMapper;
import tr.depo.backend.repository.EnvanterRepository;
import tr.depo.backend.service.DemirbasService;
import tr.depo.backend.service.EnvanterService;
import tr.depo.backend.service.SilmeService;
import tr.depo.backend.service.ZimmetGecmisiService;
import tr.depo.backend.util.SiralamaDogrulayici;
import tr.depo.domain.envanter.Envanter;
import tr.depo.domain.ortak.Sicil;
import tr.depo.domain.tanim.MalzemeTuru;

@Service
@Transactional(readOnly = true)
public class DemirbasServiceImpl extends AbstractSoftDeleteServiceImpl<Envanter> implements DemirbasService {

    static final String KAYNAK = "Demirbaş";
    static final String DEMIRBAS_NO_TEKIL = "demirbas.demirbas-no-tekil";
    static final String SERI_NO_TEKIL = "demirbas.seri-no-tekil";
    static final Set<String> SIRALAMA_ALANLARI = Set.of("id", "demirbasNo", "seriNo", "ad", "kayitTarihi", "olusturmaZamani");
    /** Barkod/seri no legacy'de tekrarlı olabilir; bulma cevabı en fazla bu kadar kayıt döner. */
    static final int BUL_EN_FAZLA = 100;

    private final EnvanterRepository repository;
    private final DemirbasMapper mapper;
    private final EnvanterService envanterService;
    private final ZimmetGecmisiService zimmetGecmisiService;

    public DemirbasServiceImpl(EnvanterRepository repository, DemirbasMapper mapper, EnvanterService envanterService,
                               ZimmetGecmisiService zimmetGecmisiService, SilmeService silmeService) {
        super(repository, silmeService, KAYNAK);
        this.repository = repository;
        this.mapper = mapper;
        this.envanterService = envanterService;
        this.zimmetGecmisiService = zimmetGecmisiService;
    }

    @Override
    public DemirbasResponse getir(Long id) {
        return mapper.cevap(aktifBul(id));
    }

    @Override
    public SayfaResponse<DemirbasResponse> listele(DemirbasFiltreRequest filtre, Pageable sayfa) {
        Sicil sicil = null;
        if (filtre.zimmetliSicil() != null) {
            sicil = Sicil.varsa(filtre.zimmetliSicil());
            if (sicil == null) {
                throw new GecersizIstekException(HataKodu.GECERSIZ_ISTEK, "zimmetliSicil boş");
            }
        }
        SiralamaDogrulayici.dogrula(sayfa, SIRALAMA_ALANLARI);
        return SayfaResponse.of(envanterService.ara(EnvanterAramaKriteri.demirbas(filtre, sicil), sayfa)
            .map(mapper::cevap));
    }

    @Override
    public List<DemirbasResponse> bul(String barkod, String seriNo, Long demirbasNo) {
        EnvanterAramaKriteri kriter = EnvanterAramaKriteri.demirbasBul(barkod, seriNo, demirbasNo);
        long verilen = Stream.of(kriter.barkod(), kriter.seriNo(), kriter.demirbasNo()).filter(Objects::nonNull).count();
        if (verilen != 1) {
            throw new GecersizIstekException(HataKodu.GECERSIZ_ISTEK, "barkod, seriNo, demirbasNo'dan tam biri verilmeli");
        }
        return envanterService.bul(kriter, BUL_EN_FAZLA).stream().map(mapper::cevap).toList();
    }

    @Override
    @Transactional
    public DemirbasResponse olustur(CreateDemirbasRequest istek) {
        envanterService.garantiSirasiniDogrula(istek.garantiBaslangic(), istek.garantiBitis());
        tekilOlmali(null, istek.demirbasNo(), istek.seriNo());
        Envanter demirbas = mapper.yeni(istek);
        envanterService.bagla(demirbas, istek.kategoriId(), istek.markaId(), istek.modelId(), istek.depoId(), istek.ihaleId());
        Envanter kaydedilen = repository.save(demirbas);
        zimmetGecmisiService.degisiklikVarsaKaydet(kaydedilen, null);
        repository.flush();
        return mapper.cevap(kaydedilen);
    }

    @Override
    @Transactional
    public DemirbasResponse guncelle(Long id, UpdateDemirbasRequest istek) {
        Envanter demirbas = aktifBul(id);
        surumuDogrula(demirbas, istek.versiyon());
        envanterService.garantiSirasiniDogrula(istek.garantiBaslangic(), istek.garantiBitis());
        tekilOlmali(demirbas, istek.demirbasNo(), istek.seriNo());
        Sicil oncekiZimmetliSicil = demirbas.getZimmetliSicil();
        mapper.guncelle(istek, demirbas);
        envanterService.guncellemedeBagla(demirbas, istek.kategoriId(), istek.markaId(), istek.modelId(), istek.depoId(),
            istek.ihaleId());
        zimmetGecmisiService.degisiklikVarsaKaydet(demirbas, oncekiZimmetliSicil);
        repository.flush();
        return mapper.cevap(demirbas);
    }

    /**
     * Demirbaş no ve seri no silinmemiş demirbaşlar arasında tekil olmalı (409). DB'de unique yok çünkü legacy'de
     * tekrarlı olabilir (HANDOFF 6.1); bu yüzden yalnızca yeni değer kontrol edilir: kayıt kendi değerini (legacy
     * tekrarı olsa bile) değiştirmeden güncellenebilir. Entity değişmeden önce sorgulanır ki otomatik flush kaydın
     * kendisini bulmasın.
     *
     * @param mevcut güncellenen kayıt; yeni kayıtta {@code null}
     */
    private void tekilOlmali(Envanter mevcut, Long demirbasNo, String seriNo) {
        boolean yeniDemirbasNo = demirbasNo != null && (mevcut == null || !demirbasNo.equals(mevcut.getDemirbasNo()));
        if (yeniDemirbasNo && repository.existsByTurAndDemirbasNo(MalzemeTuru.DEMIRBAS, demirbasNo)) {
            throw new TekillikIhlaliException(DEMIRBAS_NO_TEKIL, "ENVANTER.DEMIRBAS_NO");
        }
        boolean yeniSeriNo = seriNo != null && !seriNo.isBlank()
            && (mevcut == null || !seriNo.equals(mevcut.getSeriNo()));
        if (yeniSeriNo && repository.existsByTurAndSeriNo(MalzemeTuru.DEMIRBAS, seriNo)) {
            throw new TekillikIhlaliException(SERI_NO_TEKIL, "ENVANTER.SERI_NO");
        }
    }

    /** Aynı tabloyu sarf malzemeleriyle paylaşır: bu kaynaktan yalnızca demirbaşlar görünür. */
    @Override
    protected Envanter aktifBul(Long id) {
        Envanter envanter = super.aktifBul(id);
        if (!envanter.demirbasMi()) {
            throw new KayitBulunamadiException(KAYNAK, id);
        }
        return envanter;
    }

    @Override
    protected void altKayitlariSil(Envanter envanter, String silen) {
        envanterService.altKayitlariSil(envanter, silen);
    }
}
