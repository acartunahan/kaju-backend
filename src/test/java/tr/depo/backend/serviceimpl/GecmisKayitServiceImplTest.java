package tr.depo.backend.serviceimpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tr.depo.backend.dto.CreateEnvanterHareketiRequest;
import tr.depo.backend.dto.CreateZimmetKaydiRequest;
import tr.depo.backend.exception.IsKuraliException;
import tr.depo.backend.exception.KayitBulunamadiException;
import tr.depo.backend.exception.ReferansGecersizException;
import tr.depo.backend.mapper.DegerNesnesiMapper;
import tr.depo.backend.mapper.EnvanterHareketiMapperImpl;
import tr.depo.backend.mapper.IliskiOzetiMapper;
import tr.depo.backend.mapper.ZimmetKaydiMapperImpl;
import tr.depo.backend.repository.EnvanterHareketiRepository;
import tr.depo.backend.repository.ZimmetKaydiRepository;
import tr.depo.backend.service.ReferansService;
import tr.depo.domain.envanter.Envanter;
import tr.depo.domain.envanter.EnvanterHareketi;
import tr.depo.domain.envanter.ZimmetIslemi;
import tr.depo.domain.envanter.ZimmetKaydi;
import tr.depo.domain.tanim.MalzemeTuru;

/** Geçmiş kayıtları (hareket, zimmet): türe göre kurallar ve silinmiş kaydın 404 olması. */
@ExtendWith(MockitoExtension.class)
class GecmisKayitServiceImplTest {

    private static final LocalDateTime TARIH = LocalDateTime.of(2026, 1, 1, 0, 0);

    @Mock
    private EnvanterHareketiRepository hareketRepository;
    @Mock
    private ZimmetKaydiRepository zimmetRepository;
    @Mock
    private ReferansService referansService;

    private EnvanterHareketiServiceImpl hareketServisi() {
        return new EnvanterHareketiServiceImpl(hareketRepository,
            new EnvanterHareketiMapperImpl(new IliskiOzetiMapper(), new DegerNesnesiMapper()), referansService);
    }

    private static CreateEnvanterHareketiRequest hareket(MalzemeTuru tur, Long envanterId, BigDecimal hareketNo) {
        return new CreateEnvanterHareketiRequest(tur, envanterId, null, TARIH, null, null, null, null, null, null,
            hareketNo, null, null, null, null, null, null);
    }

    @Test
    void shouldRequireMovementNumberForDemirbasMovement() {
        assertThatThrownBy(() -> hareketServisi().olustur(hareket(MalzemeTuru.DEMIRBAS, 1L, null)))
            .isInstanceOf(IsKuraliException.class);
        verify(hareketRepository, never()).save(any());
    }

    @Test
    void shouldRejectEnvanterOfAnotherType() {
        when(referansService.aktifVeAyniTurde(eq(Envanter.class), eq(1L), eq("envanterId"), eq(MalzemeTuru.SARF), any()))
            .thenThrow(ReferansGecersizException.turUyumsuz("envanterId", 1L, MalzemeTuru.SARF));

        assertThatThrownBy(() -> hareketServisi().olustur(hareket(MalzemeTuru.SARF, 1L, null)))
            .isInstanceOf(ReferansGecersizException.class);
    }

    @Test
    void shouldReturn404ForSoftDeletedMovement() {
        EnvanterHareketi silinmis = EnvanterHareketi.sarfHareketi();
        silinmis.sil("API", TARIH);
        when(hareketRepository.findById(7L)).thenReturn(Optional.of(silinmis));

        assertThatThrownBy(() -> hareketServisi().getir(7L)).isInstanceOf(KayitBulunamadiException.class);
    }

    @Test
    void shouldTrimSicilOfZimmetRecord() {
        ZimmetKaydiServiceImpl servis = new ZimmetKaydiServiceImpl(zimmetRepository,
            new ZimmetKaydiMapperImpl(new IliskiOzetiMapper(), new DegerNesnesiMapper()), referansService);
        when(zimmetRepository.save(any(ZimmetKaydi.class))).thenAnswer(cagri -> cagri.getArgument(0));

        assertThat(servis.olustur(new CreateZimmetKaydiRequest(" 0012 ", ZimmetIslemi.DUSUM, null, TARIH, null, null))
            .sicil()).isEqualTo("0012");
    }
}
