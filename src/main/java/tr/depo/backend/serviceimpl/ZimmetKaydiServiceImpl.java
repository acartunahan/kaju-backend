package tr.depo.backend.serviceimpl;

import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.depo.backend.dto.CreateZimmetKaydiRequest;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.ZimmetKaydiResponse;
import tr.depo.backend.mapper.ZimmetKaydiMapper;
import tr.depo.backend.repository.ZimmetKaydiRepository;
import tr.depo.backend.service.ReferansService;
import tr.depo.backend.service.ZimmetKaydiService;
import tr.depo.backend.util.AktifKayitUtil;
import tr.depo.backend.util.SiralamaDogrulayici;
import tr.depo.domain.envanter.ZimmetKaydi;
import tr.depo.domain.tanim.Depo;

@Service
@Transactional(readOnly = true)
public class ZimmetKaydiServiceImpl implements ZimmetKaydiService {

    static final String KAYNAK = "Zimmet kaydı";
    static final Set<String> SIRALAMA_ALANLARI = Set.of("id", "tarih", "olusturmaZamani");

    private final ZimmetKaydiRepository repository;
    private final ZimmetKaydiMapper mapper;
    private final ReferansService referansService;

    public ZimmetKaydiServiceImpl(ZimmetKaydiRepository repository, ZimmetKaydiMapper mapper,
                                  ReferansService referansService) {
        this.repository = repository;
        this.mapper = mapper;
        this.referansService = referansService;
    }

    @Override
    public ZimmetKaydiResponse getir(Long id) {
        return mapper.cevap(AktifKayitUtil.bul(repository, id, KAYNAK));
    }

    @Override
    public SayfaResponse<ZimmetKaydiResponse> listele(Pageable sayfa) {
        SiralamaDogrulayici.dogrula(sayfa, SIRALAMA_ALANLARI);
        return SayfaResponse.of(repository.findAll(sayfa).map(mapper::cevap));
    }

    @Override
    @Transactional
    public ZimmetKaydiResponse olustur(CreateZimmetKaydiRequest istek) {
        ZimmetKaydi kayit = mapper.yeni(istek);
        kayit.setDepo(referansService.aktif(Depo.class, istek.depoId(), "depoId"));
        ZimmetKaydi kaydedilen = repository.save(kayit);
        repository.flush();
        return mapper.cevap(kaydedilen);
    }
}
