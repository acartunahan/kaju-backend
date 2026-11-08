package tr.depo.backend.serviceimpl;

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
import tr.depo.backend.dto.CreateDemirbasRequest;
import tr.depo.backend.dto.UpdateDemirbasRequest;
import tr.depo.backend.exception.IsKuraliException;
import tr.depo.backend.exception.KayitBulunamadiException;
import tr.depo.backend.exception.TekillikIhlaliException;
import tr.depo.backend.mapper.BarkodMapperImpl;
import tr.depo.backend.mapper.DegerNesnesiMapper;
import tr.depo.backend.mapper.DemirbasMapperImpl;
import tr.depo.backend.mapper.IliskiOzetiMapper;
import tr.depo.backend.mapper.SarfMalzemesiMapperImpl;
import tr.depo.backend.repository.BarkodRepository;
import tr.depo.backend.repository.EnvanterRepository;
import tr.depo.backend.service.EnvanterService;
import tr.depo.backend.service.ReferansService;
import tr.depo.backend.service.SilmeService;
import tr.depo.backend.service.ZimmetGecmisiService;
import tr.depo.domain.envanter.Barkod;
import tr.depo.domain.envanter.DemirbasDurumu;
import tr.depo.domain.envanter.Envanter;
import tr.depo.domain.envanter.EnvanterAparati;
import tr.depo.domain.envanter.EnvanterFotografi;
import tr.depo.domain.envanter.EnvanterNotu;
import tr.depo.domain.ortak.Sicil;
import tr.depo.domain.tanim.MalzemeTuru;

/** Envanter servislerinin kaynağa özgü kuralları: tür ayrımı, garanti, aggregate silme, alt kaydın sahibi. */
@ExtendWith(MockitoExtension.class)
class EnvanterServiceImplTest {

    private static final LocalDateTime TARIH = LocalDateTime.of(2026, 1, 1, 0, 0);
    private static final long ID = 5L;

    @Mock
    private EnvanterRepository envanterRepository;
    @Mock
    private ReferansService referansService;
    @Mock
    private SilmeService silmeService;
    @Mock
    private ZimmetGecmisiService zimmetGecmisiService;

    private DemirbasServiceImpl demirbasServisi;
    private SarfMalzemesiServiceImpl sarfServisi;

    @BeforeEach
    void hazirla() {
        EnvanterService envanterService = new EnvanterServiceImpl(referansService, silmeService);
        demirbasServisi = new DemirbasServiceImpl(envanterRepository,
            new DemirbasMapperImpl(new IliskiOzetiMapper(), new DegerNesnesiMapper()), envanterService,
            zimmetGecmisiService, silmeService);
        sarfServisi = new SarfMalzemesiServiceImpl(envanterRepository, new SarfMalzemesiMapperImpl(new IliskiOzetiMapper()),
            envanterService, silmeService);
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
    void shouldRejectDemirbasNoOrSerialNoUsedByAnotherActiveDemirbas() {
        when(envanterRepository.existsByTurAndDemirbasNo(MalzemeTuru.DEMIRBAS, 7L)).thenReturn(true);
        assertThatThrownBy(() -> demirbasServisi.olustur(numarali(7L, null)))
            .isInstanceOf(TekillikIhlaliException.class);

        when(envanterRepository.existsByTurAndSeriNo(MalzemeTuru.DEMIRBAS, "SN-1")).thenReturn(true);
        assertThatThrownBy(() -> demirbasServisi.olustur(numarali(null, "SN-1")))
            .isInstanceOf(TekillikIhlaliException.class);
        verify(envanterRepository, never()).save(any());
    }

    @Test
    void shouldNotCheckUniquenessWhenNumbersAreUnchangedOnUpdate() {
        Envanter legacyTekrari = Envanter.demirbas(DemirbasDurumu.NORMAL, TARIH);
        legacyTekrari.setDemirbasNo(7L);
        legacyTekrari.setSeriNo("SN-1");
        when(envanterRepository.findById(ID)).thenReturn(Optional.of(legacyTekrari));

        demirbasServisi.guncelle(ID, new UpdateDemirbasRequest(0, DemirbasDurumu.NORMAL, TARIH, 7L, "SN-1", "Laptop",
            null, null, null, null, null, null, null, null, null, null, null, null, null, null));

        verify(envanterRepository, never()).existsByTurAndDemirbasNo(any(), any());
        verify(envanterRepository, never()).existsByTurAndSeriNo(any(), any());
    }

    @Test
    void shouldPassThePreviousSicilToZimmetHistoryOnCreateAndUpdate() {
        when(envanterRepository.save(any(Envanter.class))).thenAnswer(cagri -> cagri.getArgument(0));
        demirbasServisi.olustur(demirbasIstegi(null, null));
        verify(zimmetGecmisiService).degisiklikVarsaKaydet(any(Envanter.class), eq(null));

        Envanter demirbas = Envanter.demirbas(DemirbasDurumu.NORMAL, TARIH);
        demirbas.setZimmetliSicil(new Sicil("0012"));
        when(envanterRepository.findById(ID)).thenReturn(Optional.of(demirbas));
        demirbasServisi.guncelle(ID, new UpdateDemirbasRequest(0, DemirbasDurumu.NORMAL, TARIH, null, null, "Laptop",
            null, null, null, null, "0034", null, null, null, null, null, null, null, null, null));

        verify(zimmetGecmisiService).degisiklikVarsaKaydet(demirbas, new Sicil("0012"));
        assertThat(demirbas.getZimmetliSicil()).isEqualTo(new Sicil("0034"));
    }

    private static CreateDemirbasRequest numarali(Long demirbasNo, String seriNo) {
        return new CreateDemirbasRequest(DemirbasDurumu.NORMAL, TARIH, demirbasNo, seriNo, "Laptop", null, null, null,
            null, null, null, null, null, null, null, null, null, null, null);
    }

    @Test
    void shouldSoftDeleteAllAggregateChildrenBeforeMarkingEnvanter() {
        Envanter demirbas = Envanter.demirbas(DemirbasDurumu.NORMAL, TARIH);
        when(envanterRepository.findById(ID)).thenReturn(Optional.of(demirbas));
        when(silmeService.silen()).thenReturn("API");

        demirbasServisi.sil(ID);

        InOrder sira = inOrder(silmeService);
        sira.verify(silmeService).aktifAltKayitlariSil(Barkod.class, "envanter", demirbas, "API");
        sira.verify(silmeService).aktifAltKayitlariSil(EnvanterNotu.class, "envanter", demirbas, "API");
        sira.verify(silmeService).aktifAltKayitlariSil(EnvanterAparati.class, "envanter", demirbas, "API");
        sira.verify(silmeService).aktifAltKayitlariSil(EnvanterFotografi.class, "envanter", demirbas, "API");
        assertThat(demirbas.isSilindi()).isTrue();
    }

    @Test
    void shouldReturn404ForChildOfAnotherOrDeletedEnvanter() {
        BarkodRepository barkodRepository = mock(BarkodRepository.class);
        BarkodServiceImpl barkodServisi = new BarkodServiceImpl(barkodRepository, new BarkodMapperImpl(),
            envanterRepository, silmeService);
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
