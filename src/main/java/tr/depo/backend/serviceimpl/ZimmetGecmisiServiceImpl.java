package tr.depo.backend.serviceimpl;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.ZimmetGecmisiResponse;
import tr.depo.backend.exception.KayitBulunamadiException;
import tr.depo.backend.mapper.ZimmetGecmisiMapper;
import tr.depo.backend.repository.EnvanterRepository;
import tr.depo.backend.repository.ZimmetGecmisiRepository;
import tr.depo.backend.service.ZimmetGecmisiService;
import tr.depo.backend.util.AktifKayitUtil;
import tr.depo.backend.util.SiralamaDogrulayici;
import tr.depo.domain.envanter.Envanter;
import tr.depo.domain.envanter.ZimmetGecmisi;
import tr.depo.domain.ortak.Sicil;

@Service
@Transactional(readOnly = true)
public class ZimmetGecmisiServiceImpl implements ZimmetGecmisiService {

    static final String DEMIRBAS_KAYNAGI = "Demirbaş";
    static final Set<String> SIRALAMA_ALANLARI = Set.of("id", "degisimZamani");
    private static final Sort VARSAYILAN_SIRALAMA = Sort.by(Sort.Direction.DESC, "degisimZamani", "id");

    private final ZimmetGecmisiRepository repository;
    private final EnvanterRepository envanterRepository;
    private final ZimmetGecmisiMapper mapper;
    private final Clock saat;

    public ZimmetGecmisiServiceImpl(ZimmetGecmisiRepository repository, EnvanterRepository envanterRepository,
                                    ZimmetGecmisiMapper mapper, Clock saat) {
        this.repository = repository;
        this.envanterRepository = envanterRepository;
        this.mapper = mapper;
        this.saat = saat;
    }

    @Override
    @Transactional
    public void degisiklikVarsaKaydet(Envanter demirbas, Sicil oncekiSicil) {
        Sicil yeniSicil = demirbas.getZimmetliSicil();
        if (Objects.equals(oncekiSicil, yeniSicil)) {
            return;
        }
        repository.save(ZimmetGecmisi.degisiklik(demirbas, oncekiSicil, yeniSicil, LocalDateTime.now(saat)));
    }

    @Override
    public SayfaResponse<ZimmetGecmisiResponse> listele(Long demirbasId, Pageable sayfa) {
        Envanter demirbas = AktifKayitUtil.bul(envanterRepository, demirbasId, DEMIRBAS_KAYNAGI);
        if (!demirbas.demirbasMi()) {
            throw new KayitBulunamadiException(DEMIRBAS_KAYNAGI, demirbasId);
        }
        SiralamaDogrulayici.dogrula(sayfa, SIRALAMA_ALANLARI);
        Pageable sirali = sayfa.getSort().isSorted()
            ? sayfa
            : PageRequest.of(sayfa.getPageNumber(), sayfa.getPageSize(), VARSAYILAN_SIRALAMA);
        return SayfaResponse.of(repository.findByEnvanter_Id(demirbasId, sirali).map(mapper::cevap));
    }
}
