package tr.depo.backend.common.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tr.depo.backend.common.exception.KayitBulunamadiException;
import tr.depo.backend.common.exception.KayitKullanimdaException;
import tr.depo.backend.common.exception.SurumCakismasiException;
import tr.depo.backend.common.persistence.AktifKayitRepository;
import tr.depo.domain.envanter.Envanter;
import tr.depo.domain.tanim.Depo;

@ExtendWith(MockitoExtension.class)
class SoftDeleteServisSablonuTest {

    private static final long ID = 5L;
    private static final KullanimReferansi ENVANTER_REFERANSI = new KullanimReferansi(Envanter.class, "depo", "envanter");

    @Mock
    private AktifKayitRepository<Depo> repository;
    @Mock
    private SilmeAltyapisi silmeAltyapisi;

    private OrnekServis servis;
    private Depo depo;

    /** Şablonun hook'larını gözlemlemek için en küçük alt sınıf. */
    static final class OrnekServis extends SoftDeleteServisSablonu<Depo> {

        private final List<String> altKayitSilmeCagrilari = new ArrayList<>();

        OrnekServis(AktifKayitRepository<Depo> repository, SilmeAltyapisi silmeAltyapisi) {
            super(repository, silmeAltyapisi, "Depo");
        }

        @Override
        protected List<KullanimReferansi> kullanimReferanslari() {
            return List.of(ENVANTER_REFERANSI);
        }

        @Override
        protected void altKayitlariSil(Depo kayit, String silen) {
            assertThat(kayit.isSilindi()).as("alt kayıtlar kök işaretlenmeden önce silinir").isFalse();
            altKayitSilmeCagrilari.add(silen);
        }
    }

    @BeforeEach
    void hazirla() {
        servis = new OrnekServis(repository, silmeAltyapisi);
        depo = new Depo("Merkez");
    }

    @Test
    void shouldMarkRecordDeletedAndCascadeToChildrenWithoutPhysicalDelete() {
        when(repository.findById(ID)).thenReturn(Optional.of(depo));
        when(silmeAltyapisi.aktifReferansSayisi(ENVANTER_REFERANSI, depo)).thenReturn(0L);
        when(silmeAltyapisi.silen()).thenReturn("API");

        servis.sil(ID);

        assertThat(depo.isSilindi()).isTrue();
        assertThat(depo.getSilen()).isEqualTo("API");
        assertThat(servis.altKayitSilmeCagrilari).containsExactly("API");
        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowNotFoundWhenRecordDoesNotExist() {
        when(repository.findById(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servis.sil(ID)).isInstanceOf(KayitBulunamadiException.class);
    }

    @Test
    void shouldThrowNotFoundWhenRecordIsAlreadySoftDeleted() {
        depo.sil("baska", LocalDateTime.of(2026, 1, 1, 0, 0));
        when(repository.findById(ID)).thenReturn(Optional.of(depo));

        assertThatThrownBy(() -> servis.sil(ID)).isInstanceOf(KayitBulunamadiException.class);
        assertThat(depo.getSilen()).isEqualTo("baska");
    }

    @Test
    void shouldRejectDeleteWithConflictWhenActiveRecordsReferenceIt() {
        when(repository.findById(ID)).thenReturn(Optional.of(depo));
        when(silmeAltyapisi.aktifReferansSayisi(ENVANTER_REFERANSI, depo)).thenReturn(3L);

        assertThatThrownBy(() -> servis.sil(ID))
            .isInstanceOf(KayitKullanimdaException.class)
            .hasMessageContaining("envanter=3");
        assertThat(depo.isSilindi()).isFalse();
        assertThat(servis.altKayitSilmeCagrilari).isEmpty();
    }

    @Test
    void shouldRejectStaleVersion() {
        assertThatThrownBy(() -> servis.surumuDogrula(depo, depo.getVersiyon() + 1))
            .isInstanceOf(SurumCakismasiException.class);
        servis.surumuDogrula(depo, depo.getVersiyon());
    }
}
