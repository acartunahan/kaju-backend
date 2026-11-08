package tr.depo.backend.marka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import tr.depo.backend.common.exception.GecersizIstekException;
import tr.depo.backend.common.exception.IsKuraliException;
import tr.depo.backend.common.exception.KayitBulunamadiException;
import tr.depo.backend.common.exception.KayitKullanimdaException;
import tr.depo.backend.common.exception.ReferansGecersizException;
import tr.depo.backend.common.exception.SurumCakismasiException;
import tr.depo.backend.common.mapping.IliskiOzetiEsleyici;
import tr.depo.backend.common.persistence.ReferansYukleyici;
import tr.depo.backend.common.service.KullanimReferansi;
import tr.depo.backend.common.service.SilmeAltyapisi;
import tr.depo.backend.marka.dto.MarkaResponse;
import tr.depo.backend.support.fixture.MarkaFixture;
import tr.depo.domain.tanim.Depo;
import tr.depo.domain.tanim.MalzemeTuru;
import tr.depo.domain.tanim.Marka;
import tr.depo.domain.tanim.Model;

@ExtendWith(MockitoExtension.class)
class MarkaServiceImplTest {

    private static final long ID = 10L;

    @Mock
    private MarkaRepository repository;
    @Mock
    private ReferansYukleyici referansYukleyici;
    @Mock
    private SilmeAltyapisi silmeAltyapisi;

    private MarkaServiceImpl servis;

    @BeforeEach
    void hazirla() {
        servis = new MarkaServiceImpl(repository, new MarkaMapperImpl(new IliskiOzetiEsleyici()), referansYukleyici,
            silmeAltyapisi);
    }

    private Marka kayitliMarka(MalzemeTuru tur) {
        Marka marka = new Marka(tur, "Mevcut");
        when(repository.findById(ID)).thenReturn(Optional.of(marka));
        return marka;
    }

    @Test
    void shouldReturnMarkaWhenItIsActive() {
        kayitliMarka(MalzemeTuru.DEMIRBAS);

        assertThat(servis.getir(ID).ad()).isEqualTo("Mevcut");
    }

    @Test
    void shouldThrow404WhenMarkaIsSoftDeleted() {
        kayitliMarka(MalzemeTuru.DEMIRBAS).sil("API", LocalDateTime.of(2026, 1, 1, 0, 0));

        assertThatThrownBy(() -> servis.getir(ID)).isInstanceOf(KayitBulunamadiException.class);
    }

    @Test
    void shouldCreateSarfMarkaWithDefaultActiveFlagAndLinkedDepo() {
        Depo depo = new Depo("Merkez");
        when(referansYukleyici.aktif(Depo.class, 3L, "depoId")).thenReturn(depo);
        when(repository.save(any(Marka.class))).thenAnswer(cagri -> cagri.getArgument(0));

        MarkaResponse cevap = servis.olustur(MarkaFixture.sarfMarkasi().aktif(null).depoId(3L).olusturmaIstegi());

        assertThat(cevap.tur()).isEqualTo(MalzemeTuru.SARF);
        assertThat(cevap.aktif()).isTrue();
        assertThat(cevap.depo().ad()).isEqualTo("Merkez");
        verify(repository).flush();
    }

    @Test
    void shouldRejectCreateWhenReferencedDepoIsMissingOrSoftDeleted() {
        when(referansYukleyici.aktif(eq(Depo.class), eq(99L), eq("depoId")))
            .thenThrow(new ReferansGecersizException("depoId", 99L));

        assertThatThrownBy(() -> servis.olustur(MarkaFixture.demirbasMarkasi().depoId(99L).olusturmaIstegi()))
            .isInstanceOf(ReferansGecersizException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldRejectUpdateWithStaleVersion() {
        Marka marka = kayitliMarka(MalzemeTuru.DEMIRBAS);

        assertThatThrownBy(() -> servis.guncelle(ID, MarkaFixture.demirbasMarkasi().ad("Yeni").versiyon(7).guncellemeIstegi()))
            .isInstanceOf(SurumCakismasiException.class);
        assertThat(marka.getAd()).isEqualTo("Mevcut");
    }

    @Test
    void shouldRejectClearingActiveFlagOfSarfMarka() {
        kayitliMarka(MalzemeTuru.SARF);

        assertThatThrownBy(() -> servis.guncelle(ID, MarkaFixture.sarfMarkasi().aktif(null).guncellemeIstegi()))
            .isInstanceOf(IsKuraliException.class);
    }

    @Test
    void shouldReplaceAllFieldsOnFullUpdate() {
        Marka marka = kayitliMarka(MalzemeTuru.DEMIRBAS);
        marka.setAciklama("eski açıklama");

        MarkaResponse cevap = servis.guncelle(ID, MarkaFixture.demirbasMarkasi().ad("Yeni").aciklama(null).guncellemeIstegi());

        assertThat(cevap.ad()).isEqualTo("Yeni");
        assertThat(cevap.aciklama()).as("PUT'ta gönderilmeyen alan boşaltılır").isNull();
        assertThat(cevap.depo()).isNull();
        verify(repository).flush();
    }

    @Test
    void shouldRejectSortingByNotAllowedField() {
        assertThatThrownBy(() -> servis.listele(PageRequest.of(0, 20, Sort.by("depo.ad"))))
            .isInstanceOf(GecersizIstekException.class);
        verify(repository, never()).findAll(any(PageRequest.class));
    }

    @Test
    void shouldRejectDeleteWhenActiveModelUsesMarka() {
        Marka marka = kayitliMarka(MalzemeTuru.SARF);
        when(silmeAltyapisi.aktifReferansSayisi(new KullanimReferansi(Model.class, "marka", "model"), marka))
            .thenReturn(2L);

        assertThatThrownBy(() -> servis.sil(ID)).isInstanceOf(KayitKullanimdaException.class);
        assertThat(marka.isSilindi()).isFalse();
    }

    @Test
    void shouldSoftDeleteMarkaWhenUnused() {
        Marka marka = kayitliMarka(MalzemeTuru.DEMIRBAS);
        when(silmeAltyapisi.silen()).thenReturn("API");

        servis.sil(ID);

        assertThat(marka.isSilindi()).isTrue();
        assertThat(marka.getSilen()).isEqualTo("API");
    }
}
