package tr.depo.backend.hareket;

import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.depo.backend.common.exception.IsKuraliException;
import tr.depo.backend.common.paging.SayfaCevabi;
import tr.depo.backend.common.paging.SiralamaDogrulayici;
import tr.depo.backend.common.persistence.AktifKayitlar;
import tr.depo.backend.common.persistence.ReferansYukleyici;
import tr.depo.backend.hareket.dto.CreateEnvanterHareketiRequest;
import tr.depo.backend.hareket.dto.EnvanterHareketiResponse;
import tr.depo.domain.envanter.Envanter;
import tr.depo.domain.envanter.EnvanterHareketi;
import tr.depo.domain.tanim.Depo;
import tr.depo.domain.tanim.MalzemeTuru;

@Service
@Transactional(readOnly = true)
public class EnvanterHareketiServiceImpl implements EnvanterHareketiService {

    static final String KAYNAK = "Envanter hareketi";
    static final String DEMIRBAS_HAREKET_NO_ZORUNLU = "hareket.demirbas-hareket-no-zorunlu";
    static final Set<String> SIRALAMA_ALANLARI = Set.of("id", "kayitTarihi", "olusturmaZamani");

    private final EnvanterHareketiRepository repository;
    private final EnvanterHareketiMapper mapper;
    private final ReferansYukleyici referansYukleyici;

    public EnvanterHareketiServiceImpl(EnvanterHareketiRepository repository, EnvanterHareketiMapper mapper,
                                       ReferansYukleyici referansYukleyici) {
        this.repository = repository;
        this.mapper = mapper;
        this.referansYukleyici = referansYukleyici;
    }

    @Override
    public EnvanterHareketiResponse getir(Long id) {
        return mapper.cevap(AktifKayitlar.bul(repository, id, KAYNAK));
    }

    @Override
    public SayfaCevabi<EnvanterHareketiResponse> listele(Pageable sayfa) {
        SiralamaDogrulayici.dogrula(sayfa, SIRALAMA_ALANLARI);
        return SayfaCevabi.of(repository.findAll(sayfa).map(mapper::cevap));
    }

    @Override
    @Transactional
    public EnvanterHareketiResponse olustur(CreateEnvanterHareketiRequest istek) {
        demirbasHareketNoZorunlu(istek);
        Envanter envanter = referansYukleyici.aktifVeAyniTurde(Envanter.class, istek.envanterId(), "envanterId",
            istek.tur(), Envanter::getTur);
        Depo neredenDepo = referansYukleyici.aktif(Depo.class, istek.neredenDepoId(), "neredenDepoId");
        Depo nereyeDepo = referansYukleyici.aktif(Depo.class, istek.nereyeDepoId(), "nereyeDepoId");
        Depo alanDepo = referansYukleyici.aktif(Depo.class, istek.alanDepoId(), "alanDepoId");

        EnvanterHareketi hareket = mapper.yeni(istek);
        hareket.setEnvanter(envanter);
        hareket.setNeredenDepo(neredenDepo);
        hareket.setNereyeDepo(nereyeDepo);
        hareket.setAlanDepo(alanDepo);
        EnvanterHareketi kaydedilen = repository.save(hareket);
        repository.flush();
        return mapper.cevap(kaydedilen);
    }

    /** Domain kuralı (CK_ENV_HAREKETI_DEMIRBAS): eksikse entity'ye gitmeden 422. */
    private static void demirbasHareketNoZorunlu(CreateEnvanterHareketiRequest istek) {
        if (istek.tur() == MalzemeTuru.DEMIRBAS && istek.hareketNoKodu() == null) {
            throw new IsKuraliException(DEMIRBAS_HAREKET_NO_ZORUNLU, "Demirbaş hareketinde hareketNoKodu zorunlu");
        }
    }
}
