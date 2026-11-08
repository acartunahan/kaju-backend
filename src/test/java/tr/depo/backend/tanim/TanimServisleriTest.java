package tr.depo.backend.tanim;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tr.depo.backend.common.exception.IsKuraliException;
import tr.depo.backend.common.exception.KayitKullanimdaException;
import tr.depo.backend.common.exception.ReferansGecersizException;
import tr.depo.backend.common.mapping.DegerNesnesiEsleyici;
import tr.depo.backend.common.mapping.IliskiOzetiEsleyici;
import tr.depo.backend.common.persistence.ReferansYukleyici;
import tr.depo.backend.common.service.KullanimReferansi;
import tr.depo.backend.common.service.SilmeAltyapisi;
import tr.depo.backend.depo.DepoMapperImpl;
import tr.depo.backend.depo.DepoRepository;
import tr.depo.backend.depo.DepoServiceImpl;
import tr.depo.backend.depo.dto.CreateDepoRequest;
import tr.depo.backend.firma.FirmaMapperImpl;
import tr.depo.backend.firma.FirmaRepository;
import tr.depo.backend.firma.FirmaServiceImpl;
import tr.depo.backend.firma.dto.CreateFirmaRequest;
import tr.depo.backend.firma.dto.FirmaResponse;
import tr.depo.backend.ihale.IhaleMapperImpl;
import tr.depo.backend.ihale.IhaleRepository;
import tr.depo.backend.ihale.IhaleServiceImpl;
import tr.depo.backend.ihale.dto.CreateIhaleRequest;
import tr.depo.backend.kategori.KategoriMapperImpl;
import tr.depo.backend.kategori.KategoriRepository;
import tr.depo.backend.kategori.KategoriServiceImpl;
import tr.depo.backend.kategori.dto.CreateKategoriRequest;
import tr.depo.backend.model.ModelMapperImpl;
import tr.depo.backend.model.ModelRepository;
import tr.depo.backend.model.ModelServiceImpl;
import tr.depo.backend.model.dto.CreateModelRequest;
import tr.depo.backend.model.dto.ModelResponse;
import tr.depo.domain.envanter.Envanter;
import tr.depo.domain.tanim.Depo;
import tr.depo.domain.tanim.Firma;
import tr.depo.domain.tanim.Ihale;
import tr.depo.domain.tanim.Kategori;
import tr.depo.domain.tanim.MalzemeTuru;
import tr.depo.domain.tanim.Marka;
import tr.depo.domain.tanim.Model;

/**
 * Tanım servislerinin kaynağa özgü kuralları. Ortak akış (404, sürüm, silme şablonu) Marka ve şablon testlerinde
 * sınandığı için burada tekrar edilmez.
 */
@ExtendWith(MockitoExtension.class)
class TanimServisleriTest {

    private static final LocalDateTime TARIH = LocalDateTime.of(2026, 1, 1, 0, 0);

    @Mock
    private ReferansYukleyici referansYukleyici;
    @Mock
    private SilmeAltyapisi silmeAltyapisi;

    @Nested
    class Depolar {

        @Mock
        private DepoRepository repository;

        private DepoServiceImpl servis() {
            return new DepoServiceImpl(repository, new DepoMapperImpl(new DegerNesnesiEsleyici()), silmeAltyapisi);
        }

        @Test
        void shouldTrimSicilAndTreatBlankAsEmpty() {
            when(repository.save(any(Depo.class))).thenAnswer(cagri -> cagri.getArgument(0));

            assertThat(servis().olustur(new CreateDepoRequest("Merkez", null, null, null, "  0012 ")).sorumluSicil())
                .isEqualTo("0012");
            assertThat(servis().olustur(new CreateDepoRequest("Şube", null, null, null, "   ")).sorumluSicil()).isNull();
        }

        @Test
        void shouldRejectDeleteWhenActiveEnvanterIsInDepo() {
            Depo depo = new Depo("Merkez");
            when(repository.findById(1L)).thenReturn(Optional.of(depo));
            when(silmeAltyapisi.aktifReferansSayisi(new KullanimReferansi(Envanter.class, "depo", "envanter"), depo))
                .thenReturn(5L);

            assertThatThrownBy(() -> servis().sil(1L)).isInstanceOf(KayitKullanimdaException.class);
            assertThat(depo.isSilindi()).isFalse();
        }
    }

    @Nested
    class Firmalar {

        @Mock
        private FirmaRepository repository;

        private FirmaServiceImpl servis() {
            return new FirmaServiceImpl(repository, new FirmaMapperImpl(new IliskiOzetiEsleyici()), referansYukleyici,
                silmeAltyapisi);
        }

        private static CreateFirmaRequest istek(MalzemeTuru tur, String ad, String durumKodu, LocalDateTime kayitTarihi,
                                                String yetkiliAd, String yetkiliTelefon) {
            return new CreateFirmaRequest(tur, ad, yetkiliAd, yetkiliTelefon, null, null, null, null, null, null, null,
                null, durumKodu, kayitTarihi, null);
        }

        @Test
        void shouldRequireStatusCodeAndRegistrationDateForDemirbasFirma() {
            assertThatThrownBy(() -> servis().olustur(istek(MalzemeTuru.DEMIRBAS, "X", null, TARIH, null, null)))
                .isInstanceOf(IsKuraliException.class);
            assertThatThrownBy(() -> servis().olustur(istek(MalzemeTuru.DEMIRBAS, "X", "1", null, null, null)))
                .isInstanceOf(IsKuraliException.class);
            verify(repository, never()).save(any());
        }

        @Test
        void shouldRequireNameForSarfFirma() {
            assertThatThrownBy(() -> servis().olustur(istek(MalzemeTuru.SARF, " ", null, null, null, null)))
                .isInstanceOf(IsKuraliException.class);
        }

        @Test
        void shouldMapContactPersonOnlyWhenAnyFieldIsGiven() {
            when(repository.save(any(Firma.class))).thenAnswer(cagri -> cagri.getArgument(0));

            FirmaResponse yetkiliVar = servis().olustur(istek(MalzemeTuru.SARF, "Kırtasiye", null, null, null, "0312"));
            FirmaResponse yetkiliYok = servis().olustur(istek(MalzemeTuru.DEMIRBAS, null, "1", TARIH, null, null));

            assertThat(yetkiliVar.yetkiliTelefon()).isEqualTo("0312");
            assertThat(yetkiliVar.yetkiliAd()).isNull();
            assertThat(yetkiliYok.yetkiliAd()).isNull();
            assertThat(yetkiliYok.durumKodu()).isEqualTo("1");
        }
    }

    @Nested
    class Kategoriler {

        @Mock
        private KategoriRepository repository;

        private KategoriServiceImpl servis() {
            return new KategoriServiceImpl(repository, new KategoriMapperImpl(new IliskiOzetiEsleyici()),
                referansYukleyici, silmeAltyapisi);
        }

        @Test
        void shouldRequireStatusCodeForDemirbasKategori() {
            assertThatThrownBy(() -> servis().olustur(
                    new CreateKategoriRequest(MalzemeTuru.DEMIRBAS, null, "Bilgisayar", null, null, null, null)))
                .isInstanceOf(IsKuraliException.class);
        }

        @Test
        void shouldRejectParentOfDifferentType() {
            when(referansYukleyici.aktifVeAyniTurde(eq(Kategori.class), eq(7L), eq("ustKategoriId"),
                    eq(MalzemeTuru.SARF), any()))
                .thenThrow(ReferansGecersizException.turUyumsuz("ustKategoriId", 7L, MalzemeTuru.SARF));

            assertThatThrownBy(() -> servis().olustur(
                    new CreateKategoriRequest(MalzemeTuru.SARF, 7L, "Toner", null, null, null, null)))
                .isInstanceOf(ReferansGecersizException.class);
            verify(repository, never()).save(any());
        }

        @Test
        void shouldRejectDeleteWhenActiveSubCategoryExists() {
            Kategori kategori = Kategori.sarfKategorisi("Kırtasiye");
            when(repository.findById(1L)).thenReturn(Optional.of(kategori));
            when(silmeAltyapisi.aktifReferansSayisi(new KullanimReferansi(Kategori.class, "ust", "alt kategori"), kategori))
                .thenReturn(1L);

            assertThatThrownBy(() -> servis().sil(1L)).isInstanceOf(KayitKullanimdaException.class);
        }
    }

    @Nested
    class Modeller {

        @Mock
        private ModelRepository repository;

        private ModelServiceImpl servis() {
            return new ModelServiceImpl(repository, new ModelMapperImpl(new IliskiOzetiEsleyici()), referansYukleyici,
                silmeAltyapisi);
        }

        @Test
        void shouldRequireBrandAndDefaultActiveFlagToTrueForNewSarfModel() {
            assertThatThrownBy(() -> servis().olustur(
                    new CreateModelRequest(MalzemeTuru.SARF, null, null, "Toner 85A", null, null, null, true, null)))
                .isInstanceOf(IsKuraliException.class);

            when(referansYukleyici.aktifVeAyniTurde(eq(Marka.class), eq(3L), eq("markaId"), eq(MalzemeTuru.SARF), any()))
                .thenReturn(new Marka(MalzemeTuru.SARF, "HP"));
            when(repository.save(any(Model.class))).thenAnswer(cagri -> cagri.getArgument(0));
            assertThat(servis().olustur(
                    new CreateModelRequest(MalzemeTuru.SARF, 3L, null, "Toner 85A", null, null, null, null, null))
                .aktif()).isTrue();
        }

        @Test
        void shouldCreateSarfModelWithResolvedBrand() {
            Marka marka = new Marka(MalzemeTuru.SARF, "HP");
            when(referansYukleyici.aktifVeAyniTurde(eq(Marka.class), eq(3L), eq("markaId"), eq(MalzemeTuru.SARF), any()))
                .thenReturn(marka);
            when(repository.save(any(Model.class))).thenAnswer(cagri -> cagri.getArgument(0));

            ModelResponse cevap = servis().olustur(
                new CreateModelRequest(MalzemeTuru.SARF, 3L, null, "Toner 85A", null, null, null, false, null));

            assertThat(cevap.marka().ad()).isEqualTo("HP");
            assertThat(cevap.aktif()).isFalse();
        }
    }

    @Nested
    class Ihaleler {

        @Mock
        private IhaleRepository repository;

        @Test
        void shouldRejectSarfFirma() {
            IhaleServiceImpl servis = new IhaleServiceImpl(repository, new IhaleMapperImpl(new IliskiOzetiEsleyici()),
                referansYukleyici, silmeAltyapisi);
            when(referansYukleyici.aktifVeAyniTurde(eq(Firma.class), eq(4L), eq("firmaId"), eq(MalzemeTuru.DEMIRBAS), any()))
                .thenThrow(ReferansGecersizException.turUyumsuz("firmaId", 4L, MalzemeTuru.DEMIRBAS));

            assertThatThrownBy(() -> servis.olustur(new CreateIhaleRequest("İhale", null, null, null, null, null, 4L,
                    null, "1", TARIH, null, null, null, null, null)))
                .isInstanceOf(ReferansGecersizException.class);
            verify(repository, never()).save(any(Ihale.class));
        }
    }
}
