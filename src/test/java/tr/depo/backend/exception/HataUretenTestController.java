package tr.depo.backend.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Set;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tr.depo.backend.util.SiralamaDogrulayici;

/** Yalnızca testte: GlobalExceptionHandler'ın her hata türünü nasıl çevirdiğini sınamak için. */
@RestController
@RequestMapping("/test/hata")
class HataUretenTestController {

    record OrnekIstek(@NotBlank(message = "{dogrulama.bos-olamaz}") @Size(max = 5, message = "{dogrulama.en-fazla}") String ad,
                      @NotNull(message = "{dogrulama.zorunlu}") Long depoId) {
    }

    @GetMapping("/bulunamadi")
    void bulunamadi() {
        throw new KayitBulunamadiException("Marka", 42L);
    }

    @GetMapping("/referans")
    void referans() {
        throw new ReferansGecersizException("depoId", 7L);
    }

    @GetMapping("/kullanimda")
    void kullanimda() {
        throw new KayitKullanimdaException("Marka", 42L, "envanter", 3);
    }

    @GetMapping("/surum")
    void surum() {
        throw new SurumCakismasiException("Marka", 42L);
    }

    @GetMapping("/iyimser-kilit")
    void iyimserKilit() {
        throw new ObjectOptimisticLockingFailureException("tr.depo.domain.tanim.Marka", 42L);
    }

    @GetMapping("/tekil")
    void tekil() {
        throw new DataIntegrityViolationException("unique", new SQLIntegrityConstraintViolationException(
            "ORA-00001: unique constraint (HEDEF.UK_ENVANTER_FOTOGRAFI_NESNE) violated", "23000", 1));
    }

    @GetMapping("/tekil-bilinmeyen")
    void tekilBilinmeyen() {
        throw new DataIntegrityViolationException("unique", new SQLIntegrityConstraintViolationException(
            "ORA-00001: unique constraint (HEDEF.UK_TANIMSIZ) violated", "23000", 1));
    }

    @GetMapping("/fiziksel-silme")
    void fizikselSilme() {
        throw new DataIntegrityViolationException("trigger", new SQLIntegrityConstraintViolationException(
            "ORA-20001: Fiziksel silme yasak: MARKA", "72000", 20001));
    }

    @GetMapping("/beklenmeyen")
    void beklenmeyen() {
        throw new IllegalStateException("gizli iç ayrıntı: şifre=xyz");
    }

    @GetMapping("/siralama")
    void siralama(Pageable sayfa) {
        SiralamaDogrulayici.dogrula(sayfa, Set.of("ad", "id"));
    }

    @PostMapping("/dogrulama")
    void dogrulama(@Valid @RequestBody OrnekIstek istek) {
        // Geçerli istekte bir şey yapmaz.
    }
}
