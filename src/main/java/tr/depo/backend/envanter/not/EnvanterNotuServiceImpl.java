package tr.depo.backend.envanter.not;

import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.depo.backend.common.paging.SayfaCevabi;
import tr.depo.backend.common.paging.SiralamaDogrulayici;
import tr.depo.backend.common.service.SilmeAltyapisi;
import tr.depo.backend.envanter.EnvanterAltKayitServisi;
import tr.depo.backend.envanter.EnvanterRepository;
import tr.depo.backend.envanter.not.dto.CreateEnvanterNotuRequest;
import tr.depo.backend.envanter.not.dto.EnvanterNotuResponse;
import tr.depo.backend.envanter.not.dto.UpdateEnvanterNotuRequest;
import tr.depo.domain.envanter.Envanter;
import tr.depo.domain.envanter.EnvanterNotu;

@Service
@Transactional(readOnly = true)
public class EnvanterNotuServiceImpl extends EnvanterAltKayitServisi<EnvanterNotu> implements EnvanterNotuService {

    static final String KAYNAK = "Not";
    static final Set<String> SIRALAMA_ALANLARI = Set.of("id", "kayitTarihi", "olusturmaZamani");

    private final EnvanterNotuRepository repository;
    private final EnvanterNotuMapper mapper;

    public EnvanterNotuServiceImpl(EnvanterNotuRepository repository, EnvanterNotuMapper mapper,
                                   EnvanterRepository envanterRepository, SilmeAltyapisi silmeAltyapisi) {
        super(repository, envanterRepository, silmeAltyapisi, KAYNAK);
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public EnvanterNotuResponse getir(Long envanterId, Long id) {
        return mapper.cevap(aktifAltKayit(envanterId, id));
    }

    @Override
    public SayfaCevabi<EnvanterNotuResponse> listele(Long envanterId, Pageable sayfa) {
        SiralamaDogrulayici.dogrula(sayfa, SIRALAMA_ALANLARI);
        aktifUst(envanterId);
        return SayfaCevabi.of(repository.findByEnvanter_Id(envanterId, sayfa).map(mapper::cevap));
    }

    @Override
    @Transactional
    public EnvanterNotuResponse olustur(Long envanterId, CreateEnvanterNotuRequest istek) {
        EnvanterNotu not = mapper.yeni(istek);
        not.setEnvanter(aktifUst(envanterId));
        EnvanterNotu kaydedilen = repository.save(not);
        repository.flush();
        return mapper.cevap(kaydedilen);
    }

    @Override
    @Transactional
    public EnvanterNotuResponse guncelle(Long envanterId, Long id, UpdateEnvanterNotuRequest istek) {
        EnvanterNotu not = aktifAltKayit(envanterId, id);
        surumuDogrula(not, istek.versiyon());
        mapper.guncelle(istek, not);
        repository.flush();
        return mapper.cevap(not);
    }

    @Override
    protected Envanter ustKaydi(EnvanterNotu not) {
        return not.getEnvanter();
    }
}
