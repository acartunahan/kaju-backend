package tr.depo.backend.envanter.not;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tr.depo.backend.common.paging.SayfaCevabi;
import tr.depo.backend.common.web.KaynakKonumu;
import tr.depo.backend.envanter.EnvanterYollari;
import tr.depo.backend.envanter.not.dto.CreateEnvanterNotuRequest;
import tr.depo.backend.envanter.not.dto.EnvanterNotuResponse;
import tr.depo.backend.envanter.not.dto.UpdateEnvanterNotuRequest;

@RestController
@RequestMapping(EnvanterNotuController.YOL)
@Tag(name = "Envanter notları", description = "Demirbaş ya da sarf malzemesine eklenen notlar")
public class EnvanterNotuController {

    static final String YOL = EnvanterYollari.ENVANTER + "/notlar";

    private final EnvanterNotuService notService;

    public EnvanterNotuController(EnvanterNotuService notService) {
        this.notService = notService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Notu getirir")
    public EnvanterNotuResponse getir(@PathVariable Long envanterId, @PathVariable Long id) {
        return notService.getir(envanterId, id);
    }

    @GetMapping
    @Operation(summary = "Envanterin notlarını listeler", description = "Sıralama alanları: id, kayitTarihi, olusturmaZamani.")
    public SayfaCevabi<EnvanterNotuResponse> listele(@PathVariable Long envanterId, @ParameterObject Pageable sayfa) {
        return notService.listele(envanterId, sayfa);
    }

    @PostMapping
    @Operation(summary = "Envantere not ekler")
    public ResponseEntity<EnvanterNotuResponse> olustur(@PathVariable Long envanterId,
                                                        @Valid @RequestBody CreateEnvanterNotuRequest istek) {
        EnvanterNotuResponse olusan = notService.olustur(envanterId, istek);
        return ResponseEntity.created(KaynakKonumu.yeniKayit(olusan.id())).body(olusan);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Notu günceller (tam güncelleme)")
    public EnvanterNotuResponse guncelle(@PathVariable Long envanterId, @PathVariable Long id,
                                         @Valid @RequestBody UpdateEnvanterNotuRequest istek) {
        return notService.guncelle(envanterId, id, istek);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Notu siler (soft delete)")
    public ResponseEntity<Void> sil(@PathVariable Long envanterId, @PathVariable Long id) {
        notService.sil(envanterId, id);
        return ResponseEntity.noContent().build();
    }
}
