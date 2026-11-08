package tr.depo.backend.serviceimpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tr.depo.backend.exception.KayitBulunamadiException;
import tr.depo.backend.mapper.ArizaIslemiMapperImpl;
import tr.depo.backend.mapper.ArizaMapperImpl;
import tr.depo.backend.mapper.DegerNesnesiMapper;
import tr.depo.backend.mapper.IliskiOzetiMapper;
import tr.depo.backend.mapper.SayimMapperImpl;
import tr.depo.backend.repository.ArizaIslemiRepository;
import tr.depo.backend.repository.ArizaRepository;
import tr.depo.backend.repository.SayimRepository;
import tr.depo.backend.service.ReferansService;
import tr.depo.backend.service.SilmeService;
import tr.depo.domain.ariza.Ariza;
import tr.depo.domain.ariza.ArizaIslemTuru;
import tr.depo.domain.ariza.ArizaIslemi;
import tr.depo.domain.ortak.Sicil;
import tr.depo.domain.sayim.Sayim;
import tr.depo.domain.sayim.SayimKapsami;
import tr.depo.domain.sayim.SayimSatiri;

/** Arıza ve sayım aggregate'lerinde silmenin alt kayıtlara yayılması ve alt kaydın sahibi kontrolü. */
@ExtendWith(MockitoExtension.class)
class ArizaSayimServiceImplTest {

    private static final LocalDateTime TARIH = LocalDateTime.of(2026, 1, 1, 0, 0);

    @Mock
    private ArizaRepository arizaRepository;
    @Mock
    private SayimRepository sayimRepository;
    @Mock
    private ReferansService referansService;
    @Mock
    private SilmeService silmeService;

    private static Ariza ariza() {
        return new Ariza(new Sicil("0012345"), "Ekran", BigDecimal.ONE, TARIH);
    }

    @Test
    void shouldSoftDeleteOperationsWhenArizaIsDeleted() {
        ArizaServiceImpl servis = new ArizaServiceImpl(arizaRepository,
            new ArizaMapperImpl(new IliskiOzetiMapper(), new DegerNesnesiMapper()), referansService, silmeService);
        Ariza ariza = ariza();
        when(arizaRepository.findById(1L)).thenReturn(Optional.of(ariza));
        when(silmeService.silen()).thenReturn("API");

        servis.sil(1L);

        verify(silmeService).aktifAltKayitlariSil(ArizaIslemi.class, "ariza", ariza, "API");
        assertThat(ariza.isSilindi()).isTrue();
    }

    @Test
    void shouldSoftDeleteRowsWhenSayimIsDeleted() {
        SayimServiceImpl servis = new SayimServiceImpl(sayimRepository, new SayimMapperImpl(new IliskiOzetiMapper()),
            referansService, silmeService);
        Sayim sayim = new Sayim(SayimKapsami.GENEL, "Yıl sonu");
        when(sayimRepository.findById(2L)).thenReturn(Optional.of(sayim));
        when(silmeService.silen()).thenReturn("API");

        servis.sil(2L);

        verify(silmeService).aktifAltKayitlariSil(SayimSatiri.class, "sayim", sayim, "API");
        assertThat(sayim.isSilindi()).isTrue();
    }

    @Test
    void shouldReturn404ForOperationOfAnotherAriza() {
        ArizaIslemiRepository islemRepository = mock(ArizaIslemiRepository.class);
        ArizaIslemiServiceImpl servis = new ArizaIslemiServiceImpl(islemRepository,
            new ArizaIslemiMapperImpl(new DegerNesnesiMapper()), arizaRepository, silmeService);
        ArizaIslemi islem = new ArizaIslemi(BigDecimal.ONE, new Sicil("1"), new Sicil("2"), TARIH, TARIH,
            ArizaIslemTuru.BILGI);
        islem.setAriza(ariza());
        when(arizaRepository.findById(1L)).thenReturn(Optional.of(ariza()));
        when(islemRepository.findById(9L)).thenReturn(Optional.of(islem));

        assertThatThrownBy(() -> servis.getir(1L, 9L)).isInstanceOf(KayitBulunamadiException.class);
    }
}
