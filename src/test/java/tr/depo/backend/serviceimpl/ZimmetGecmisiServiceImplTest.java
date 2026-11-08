package tr.depo.backend.serviceimpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import tr.depo.backend.exception.KayitBulunamadiException;
import tr.depo.backend.mapper.DegerNesnesiMapper;
import tr.depo.backend.mapper.ZimmetGecmisiMapperImpl;
import tr.depo.backend.repository.EnvanterRepository;
import tr.depo.backend.repository.ZimmetGecmisiRepository;
import tr.depo.domain.envanter.DemirbasDurumu;
import tr.depo.domain.envanter.Envanter;
import tr.depo.domain.envanter.ZimmetGecmisi;
import tr.depo.domain.envanter.ZimmetGecmisiKaynagi;
import tr.depo.domain.ortak.Sicil;

/** Zimmet geçmişi: yalnızca gerçek değişiklik kaydedilir; liste yalnızca aktif demirbaş için, en yeni önce. */
@ExtendWith(MockitoExtension.class)
class ZimmetGecmisiServiceImplTest {

    private static final LocalDateTime SIMDI = LocalDateTime.of(2026, 11, 8, 12, 0);

    @Mock
    private ZimmetGecmisiRepository repository;
    @Mock
    private EnvanterRepository envanterRepository;

    private ZimmetGecmisiServiceImpl servis;
    private Envanter demirbas;

    @BeforeEach
    void hazirla() {
        Clock saat = Clock.fixed(SIMDI.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        servis = new ZimmetGecmisiServiceImpl(repository, envanterRepository, new ZimmetGecmisiMapperImpl(new DegerNesnesiMapper()), saat);
        demirbas = Envanter.demirbas(DemirbasDurumu.NORMAL, SIMDI);
    }

    @Test
    void shouldRecordPreviousAndNewSicilWithCurrentTimeWhenSicilChanges() {
        demirbas.setZimmetliSicil(new Sicil("0034"));

        servis.degisiklikVarsaKaydet(demirbas, new Sicil("0012"));

        ArgumentCaptor<ZimmetGecmisi> kayit = ArgumentCaptor.forClass(ZimmetGecmisi.class);
        verify(repository).save(kayit.capture());
        assertThat(kayit.getValue().getOncekiSicil()).isEqualTo(new Sicil("0012"));
        assertThat(kayit.getValue().getYeniSicil()).isEqualTo(new Sicil("0034"));
        assertThat(kayit.getValue().getDegisimZamani()).isEqualTo(SIMDI);
        assertThat(kayit.getValue().getKaynak()).isEqualTo(ZimmetGecmisiKaynagi.DEGISIKLIK);
    }

    @Test
    void shouldRecordNothingWhenSicilIsUnchangedOrStaysEmpty() {
        demirbas.setZimmetliSicil(new Sicil("0012"));
        servis.degisiklikVarsaKaydet(demirbas, new Sicil("0012"));

        Envanter zimmetsiz = Envanter.demirbas(DemirbasDurumu.NORMAL, SIMDI);
        servis.degisiklikVarsaKaydet(zimmetsiz, null);

        verify(repository, never()).save(any());
    }

    @Test
    void shouldReturn404ForSarfOrDeletedDemirbas() {
        when(envanterRepository.findById(1L)).thenReturn(Optional.of(Envanter.sarf("Toner")));
        assertThatThrownBy(() -> servis.listele(1L, PageRequest.of(0, 20))).isInstanceOf(KayitBulunamadiException.class);

        Envanter silinmis = Envanter.demirbas(DemirbasDurumu.NORMAL, SIMDI);
        silinmis.sil("API", SIMDI);
        when(envanterRepository.findById(2L)).thenReturn(Optional.of(silinmis));
        assertThatThrownBy(() -> servis.listele(2L, PageRequest.of(0, 20))).isInstanceOf(KayitBulunamadiException.class);
        verify(repository, never()).findByEnvanter_Id(any(), any());
    }

    @Test
    void shouldListNewestChangeFirstWhenNoSortIsGiven() {
        when(envanterRepository.findById(3L)).thenReturn(Optional.of(demirbas));
        Page<ZimmetGecmisi> bos = new PageImpl<>(List.of());
        when(repository.findByEnvanter_Id(eq(3L), any(Pageable.class))).thenReturn(bos);

        servis.listele(3L, PageRequest.of(1, 10));

        ArgumentCaptor<Pageable> sayfa = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findByEnvanter_Id(eq(3L), sayfa.capture());
        assertThat(sayfa.getValue().getPageNumber()).isEqualTo(1);
        assertThat(sayfa.getValue().getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, "degisimZamani", "id"));
    }
}
