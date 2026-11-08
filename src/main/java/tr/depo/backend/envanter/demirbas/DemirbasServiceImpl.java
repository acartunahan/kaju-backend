package tr.depo.backend.envanter.demirbas;

import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.depo.backend.common.exception.KayitBulunamadiException;
import tr.depo.backend.common.exception.TekillikIhlaliException;
import tr.depo.backend.common.paging.SayfaCevabi;
import tr.depo.backend.common.paging.SiralamaDogrulayici;
import tr.depo.backend.common.service.SilmeAltyapisi;
import tr.depo.backend.common.service.SoftDeleteServisSablonu;
import tr.depo.backend.envanter.EnvanterAltKayitlari;
import tr.depo.backend.envanter.EnvanterIliskileri;
import tr.depo.backend.envanter.EnvanterRepository;
import tr.depo.backend.envanter.demirbas.dto.CreateDemirbasRequest;
import tr.depo.backend.envanter.demirbas.dto.DemirbasResponse;
import tr.depo.backend.envanter.demirbas.dto.UpdateDemirbasRequest;
import tr.depo.domain.envanter.Envanter;
import tr.depo.domain.tanim.MalzemeTuru;

@Service
@Transactional(readOnly = true)
public class DemirbasServiceImpl extends SoftDeleteServisSablonu<Envanter> implements DemirbasService {

    static final String KAYNAK = "Demirbaş";
    static final String DEMIRBAS_NO_TEKIL = "demirbas.demirbas-no-tekil";
    static final String SERI_NO_TEKIL = "demirbas.seri-no-tekil";
    static final Set<String> SIRALAMA_ALANLARI = Set.of("id", "demirbasNo", "seriNo", "ad", "kayitTarihi", "olusturmaZamani");

    private final EnvanterRepository repository;
    private final DemirbasMapper mapper;
    private final EnvanterIliskileri iliskiler;
    private final EnvanterAltKayitlari altKayitlar;

    public DemirbasServiceImpl(EnvanterRepository repository, DemirbasMapper mapper, EnvanterIliskileri iliskiler,
                               EnvanterAltKayitlari altKayitlar, SilmeAltyapisi silmeAltyapisi) {
        super(repository, silmeAltyapisi, KAYNAK);
        this.repository = repository;
        this.mapper = mapper;
        this.iliskiler = iliskiler;
        this.altKayitlar = altKayitlar;
    }

    @Override
    public DemirbasResponse getir(Long id) {
        return mapper.cevap(aktifBul(id));
    }

    @Override
    public SayfaCevabi<DemirbasResponse> listele(Pageable sayfa) {
        SiralamaDogrulayici.dogrula(sayfa, SIRALAMA_ALANLARI);
        return SayfaCevabi.of(repository.findByTur(MalzemeTuru.DEMIRBAS, sayfa).map(mapper::cevap));
    }

    @Override
    @Transactional
    public DemirbasResponse olustur(CreateDemirbasRequest istek) {
        EnvanterIliskileri.garantiSirasiniDogrula(istek.garantiBaslangic(), istek.garantiBitis());
        tekilOlmali(null, istek.demirbasNo(), istek.seriNo());
        Envanter demirbas = mapper.yeni(istek);
        iliskiler.bagla(demirbas, istek.kategoriId(), istek.markaId(), istek.modelId(), istek.depoId(), istek.ihaleId());
        Envanter kaydedilen = repository.save(demirbas);
        repository.flush();
        return mapper.cevap(kaydedilen);
    }

    @Override
    @Transactional
    public DemirbasResponse guncelle(Long id, UpdateDemirbasRequest istek) {
        Envanter demirbas = aktifBul(id);
        surumuDogrula(demirbas, istek.versiyon());
        EnvanterIliskileri.garantiSirasiniDogrula(istek.garantiBaslangic(), istek.garantiBitis());
        tekilOlmali(demirbas, istek.demirbasNo(), istek.seriNo());
        mapper.guncelle(istek, demirbas);
        iliskiler.bagla(demirbas, istek.kategoriId(), istek.markaId(), istek.modelId(), istek.depoId(), istek.ihaleId());
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
        altKayitlar.sil(envanter, silen);
    }
}
