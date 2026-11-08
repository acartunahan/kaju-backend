package tr.depo.backend.ariza;

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
import tr.depo.backend.ariza.islem.ArizaIslemiMapperImpl;
import tr.depo.backend.ariza.islem.ArizaIslemiRepository;
import tr.depo.backend.ariza.islem.ArizaIslemiServiceImpl;
import tr.depo.backend.common.exception.KayitBulunamadiException;
import tr.depo.backend.common.mapping.DegerNesnesiEsleyici;
import tr.depo.backend.common.mapping.IliskiOzetiEsleyici;
import tr.depo.backend.common.persistence.ReferansYukleyici;
import tr.depo.backend.common.service.SilmeAltyapisi;
import tr.depo.backend.sayim.SayimMapperImpl;
import tr.depo.backend.sayim.SayimRepository;
import tr.depo.backend.sayim.SayimServiceImpl;
import tr.depo.domain.ariza.Ariza;
import tr.depo.domain.ariza.ArizaIslemTuru;
import tr.depo.domain.ariza.ArizaIslemi;
import tr.depo.domain.ortak.Sicil;
import tr.depo.domain.sayim.Sayim;
import tr.depo.domain.sayim.SayimKapsami;
import tr.depo.domain.sayim.SayimSatiri;

/** Arıza ve sayım aggregate'lerinde silmenin alt kayıtlara yayılması ve alt kaydın sahibi kontrolü. */
@ExtendWith(MockitoExtension.class)
class ArizaSayimServisleriTest {

    private static final LocalDateTime TARIH = LocalDateTime.of(2026, 1, 1, 0, 0);

    @Mock
    private ArizaRepository arizaRepository;
    @Mock
    private SayimRepository sayimRepository;
    @Mock
    private ReferansYukleyici referansYukleyici;
    @Mock
    private SilmeAltyapisi silmeAltyapisi;

    private static Ariza ariza() {
        return new Ariza(new Sicil("0012345"), "Ekran", BigDecimal.ONE, TARIH);
    }

    @Test
    void shouldSoftDeleteOperationsWhenArizaIsDeleted() {
        ArizaServiceImpl servis = new ArizaServiceImpl(arizaRepository,
            new ArizaMapperImpl(new IliskiOzetiEsleyici(), new DegerNesnesiEsleyici()), referansYukleyici, silmeAltyapisi);
        Ariza ariza = ariza();
        when(arizaRepository.findById(1L)).thenReturn(Optional.of(ariza));
        when(silmeAltyapisi.silen()).thenReturn("API");

        servis.sil(1L);

        verify(silmeAltyapisi).aktifAltKayitlariSil(ArizaIslemi.class, "ariza", ariza, "API");
        assertThat(ariza.isSilindi()).isTrue();
    }

    @Test
    void shouldSoftDeleteRowsWhenSayimIsDeleted() {
        SayimServiceImpl servis = new SayimServiceImpl(sayimRepository, new SayimMapperImpl(new IliskiOzetiEsleyici()),
            referansYukleyici, silmeAltyapisi);
        Sayim sayim = new Sayim(SayimKapsami.GENEL, "Yıl sonu");
        when(sayimRepository.findById(2L)).thenReturn(Optional.of(sayim));
        when(silmeAltyapisi.silen()).thenReturn("API");

        servis.sil(2L);

        verify(silmeAltyapisi).aktifAltKayitlariSil(SayimSatiri.class, "sayim", sayim, "API");
        assertThat(sayim.isSilindi()).isTrue();
    }

    @Test
    void shouldReturn404ForOperationOfAnotherAriza() {
        ArizaIslemiRepository islemRepository = mock(ArizaIslemiRepository.class);
        ArizaIslemiServiceImpl servis = new ArizaIslemiServiceImpl(islemRepository,
            new ArizaIslemiMapperImpl(new DegerNesnesiEsleyici()), arizaRepository, silmeAltyapisi);
        ArizaIslemi islem = new ArizaIslemi(BigDecimal.ONE, new Sicil("1"), new Sicil("2"), TARIH, TARIH,
            ArizaIslemTuru.BILGI);
        islem.setAriza(ariza());
        when(arizaRepository.findById(1L)).thenReturn(Optional.of(ariza()));
        when(islemRepository.findById(9L)).thenReturn(Optional.of(islem));

        assertThatThrownBy(() -> servis.getir(1L, 9L)).isInstanceOf(KayitBulunamadiException.class);
    }
}
