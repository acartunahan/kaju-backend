package tr.depo.backend.zimmet;

import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.depo.backend.common.paging.SayfaCevabi;
import tr.depo.backend.common.paging.SiralamaDogrulayici;
import tr.depo.backend.common.persistence.AktifKayitlar;
import tr.depo.backend.common.persistence.ReferansYukleyici;
import tr.depo.backend.zimmet.dto.CreateZimmetKaydiRequest;
import tr.depo.backend.zimmet.dto.ZimmetKaydiResponse;
import tr.depo.domain.envanter.ZimmetKaydi;
import tr.depo.domain.tanim.Depo;

@Service
@Transactional(readOnly = true)
public class ZimmetKaydiServiceImpl implements ZimmetKaydiService {

    static final String KAYNAK = "Zimmet kaydı";
    static final Set<String> SIRALAMA_ALANLARI = Set.of("id", "tarih", "olusturmaZamani");

    private final ZimmetKaydiRepository repository;
    private final ZimmetKaydiMapper mapper;
    private final ReferansYukleyici referansYukleyici;

    public ZimmetKaydiServiceImpl(ZimmetKaydiRepository repository, ZimmetKaydiMapper mapper,
                                  ReferansYukleyici referansYukleyici) {
        this.repository = repository;
        this.mapper = mapper;
        this.referansYukleyici = referansYukleyici;
    }

    @Override
    public ZimmetKaydiResponse getir(Long id) {
        return mapper.cevap(AktifKayitlar.bul(repository, id, KAYNAK));
    }

    @Override
    public SayfaCevabi<ZimmetKaydiResponse> listele(Pageable sayfa) {
        SiralamaDogrulayici.dogrula(sayfa, SIRALAMA_ALANLARI);
        return SayfaCevabi.of(repository.findAll(sayfa).map(mapper::cevap));
    }

    @Override
    @Transactional
    public ZimmetKaydiResponse olustur(CreateZimmetKaydiRequest istek) {
        ZimmetKaydi kayit = mapper.yeni(istek);
        kayit.setDepo(referansYukleyici.aktif(Depo.class, istek.depoId(), "depoId"));
        ZimmetKaydi kaydedilen = repository.save(kayit);
        repository.flush();
        return mapper.cevap(kaydedilen);
    }
}
