package tr.depo.backend.envanter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import tr.depo.backend.common.exception.IsKuraliException;
import tr.depo.backend.common.exception.KayitBulunamadiException;
import tr.depo.backend.common.mapping.DegerNesnesiEsleyici;
import tr.depo.backend.common.mapping.IliskiOzetiEsleyici;
import tr.depo.backend.common.persistence.ReferansYukleyici;
import tr.depo.backend.common.service.SilmeAltyapisi;
import tr.depo.backend.envanter.barkod.BarkodMapperImpl;
import tr.depo.backend.envanter.barkod.BarkodRepository;
import tr.depo.backend.envanter.barkod.BarkodServiceImpl;
import tr.depo.backend.envanter.demirbas.DemirbasMapperImpl;
import tr.depo.backend.envanter.demirbas.DemirbasServiceImpl;
import tr.depo.backend.envanter.demirbas.dto.CreateDemirbasRequest;
import tr.depo.backend.envanter.sarf.SarfMalzemesiMapperImpl;
import tr.depo.backend.envanter.sarf.SarfMalzemesiServiceImpl;
import tr.depo.domain.envanter.Barkod;
import tr.depo.domain.envanter.DemirbasDurumu;
import tr.depo.domain.envanter.Envanter;
import tr.depo.domain.envanter.EnvanterAparati;
import tr.depo.domain.envanter.EnvanterFotografi;
import tr.depo.domain.envanter.EnvanterNotu;

/** Envanter servislerinin kaynağa özgü kuralları: tür ayrımı, garanti, aggregate silme, alt kaydın sahibi. */
@ExtendWith(MockitoExtension.class)
class EnvanterServisleriTest {

    private static final LocalDateTime TARIH = LocalDateTime.of(2026, 1, 1, 0, 0);
    private static final long ID = 5L;

    @Mock
    private EnvanterRepository envanterRepository;
    @Mock
    private ReferansYukleyici referansYukleyici;
    @Mock
    private SilmeAltyapisi silmeAltyapisi;

    private DemirbasServiceImpl demirbasServisi;
    private SarfMalzemesiServiceImpl sarfServisi;

    @BeforeEach
    void hazirla() {
        EnvanterIliskileri iliskiler = new EnvanterIliskileri(referansYukleyici);
        EnvanterAltKayitlari altKayitlar = new EnvanterAltKayitlari(silmeAltyapisi);
        demirbasServisi = new DemirbasServiceImpl(envanterRepository,
            new DemirbasMapperImpl(new IliskiOzetiEsleyici(), new DegerNesnesiEsleyici()), iliskiler, altKayitlar,
            silmeAltyapisi);
        sarfServisi = new SarfMalzemesiServiceImpl(envanterRepository, new SarfMalzemesiMapperImpl(new IliskiOzetiEsleyici()),
            iliskiler, altKayitlar, silmeAltyapisi);
    }

    private static CreateDemirbasRequest demirbasIstegi(LocalDateTime garantiBaslangic, LocalDateTime garantiBitis) {
        return new CreateDemirbasRequest(DemirbasDurumu.NORMAL, TARIH, null, null, "Laptop", null, null, null, null,
            null, null, null, garantiBaslangic, garantiBitis, null, null, null, null, null);
    }

    @Test
    void shouldReturn404WhenSarfIsRequestedThroughDemirbasResourceAndViceVersa() {
        when(envanterRepository.findById(ID)).thenReturn(Optional.of(Envanter.sarf("Toner")));
        assertThatThrownBy(() -> demirbasServisi.getir(ID)).isInstanceOf(KayitBulunamadiException.class);
        assertThatThrownBy(() -> demirbasServisi.sil(ID)).isInstanceOf(KayitBulunamadiException.class);

        when(envanterRepository.findById(ID)).thenReturn(Optional.of(Envanter.demirbas(DemirbasDurumu.NORMAL, TARIH)));
        assertThatThrownBy(() -> sarfServisi.getir(ID)).isInstanceOf(KayitBulunamadiException.class);
    }

    @Test
    void shouldRejectWarrantyEndBeforeStart() {
        assertThatThrownBy(() -> demirbasServisi.olustur(demirbasIstegi(TARIH, TARIH.minusDays(1))))
            .isInstanceOf(IsKuraliException.class);
        verify(envanterRepository, never()).save(any());
    }

    @Test
    void shouldAcceptOpenEndedWarranty() {
        when(envanterRepository.save(any(Envanter.class))).thenAnswer(cagri -> cagri.getArgument(0));

        assertThat(demirbasServisi.olustur(demirbasIstegi(TARIH, null)).garantiBaslangic()).isEqualTo(TARIH);
    }

    @Test
    void shouldSoftDeleteAllAggregateChildrenBeforeMarkingEnvanter() {
        Envanter demirbas = Envanter.demirbas(DemirbasDurumu.NORMAL, TARIH);
        when(envanterRepository.findById(ID)).thenReturn(Optional.of(demirbas));
        when(silmeAltyapisi.silen()).thenReturn("API");

        demirbasServisi.sil(ID);

        InOrder sira = inOrder(silmeAltyapisi);
        sira.verify(silmeAltyapisi).aktifAltKayitlariSil(Barkod.class, "envanter", demirbas, "API");
        sira.verify(silmeAltyapisi).aktifAltKayitlariSil(EnvanterNotu.class, "envanter", demirbas, "API");
        sira.verify(silmeAltyapisi).aktifAltKayitlariSil(EnvanterAparati.class, "envanter", demirbas, "API");
        sira.verify(silmeAltyapisi).aktifAltKayitlariSil(EnvanterFotografi.class, "envanter", demirbas, "API");
        assertThat(demirbas.isSilindi()).isTrue();
    }

    @Test
    void shouldReturn404ForChildOfAnotherOrDeletedEnvanter() {
        BarkodRepository barkodRepository = mock(BarkodRepository.class);
        BarkodServiceImpl barkodServisi = new BarkodServiceImpl(barkodRepository, new BarkodMapperImpl(),
            envanterRepository, silmeAltyapisi);
        Envanter baskaEnvanter = Envanter.demirbas(DemirbasDurumu.NORMAL, TARIH);
        Barkod barkod = new Barkod("8690000000001", false);
        barkod.setEnvanter(baskaEnvanter);
        when(envanterRepository.findById(eq(1L))).thenReturn(Optional.of(Envanter.demirbas(DemirbasDurumu.NORMAL, TARIH)));
        when(barkodRepository.findById(ID)).thenReturn(Optional.of(barkod));

        assertThatThrownBy(() -> barkodServisi.getir(1L, ID)).as("barkod başka envantere ait")
            .isInstanceOf(KayitBulunamadiException.class);

        Envanter silinmis = Envanter.demirbas(DemirbasDurumu.NORMAL, TARIH);
        silinmis.sil("API", TARIH);
        when(envanterRepository.findById(eq(2L))).thenReturn(Optional.of(silinmis));
        assertThatThrownBy(() -> barkodServisi.listele(2L, PageRequest.of(0, 20)))
            .as("envanter silinmiş").isInstanceOf(KayitBulunamadiException.class)
            .hasMessageContaining("Envanter");
    }
}
