package tr.depo.backend.controller;

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
import tr.depo.backend.dto.CreateEnvanterAparatiRequest;
import tr.depo.backend.dto.EnvanterAparatiResponse;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.UpdateEnvanterAparatiRequest;
import tr.depo.backend.service.EnvanterAparatiService;
import tr.depo.backend.util.KaynakKonumu;

@RestController
@RequestMapping(EnvanterAparatiController.YOL)
@Tag(name = "Envanter aparatları", description = "Demirbaşla birlikte gelen aparatlar (bir envanterin birden fazla aparatı olabilir)")
public class EnvanterAparatiController {

    static final String YOL = ApiPaths.ENVANTER + "/aparatlar";

    private final EnvanterAparatiService aparatService;

    public EnvanterAparatiController(EnvanterAparatiService aparatService) {
        this.aparatService = aparatService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Aparatı getirir")
    public EnvanterAparatiResponse getir(@PathVariable Long envanterId, @PathVariable Long id) {
        return aparatService.getir(envanterId, id);
    }

    @GetMapping
    @Operation(summary = "Envanterin aparatlarını listeler", description = "Sıralama alanları: id, ad, olusturmaZamani.")
    public SayfaResponse<EnvanterAparatiResponse> listele(@PathVariable Long envanterId, @ParameterObject Pageable sayfa) {
        return aparatService.listele(envanterId, sayfa);
    }

    @PostMapping
    @Operation(summary = "Envantere aparat ekler", description = "adet en az 1.")
    public ResponseEntity<EnvanterAparatiResponse> olustur(@PathVariable Long envanterId,
                                                           @Valid @RequestBody CreateEnvanterAparatiRequest istek) {
        EnvanterAparatiResponse olusan = aparatService.olustur(envanterId, istek);
        return ResponseEntity.created(KaynakKonumu.yeniKayit(olusan.id())).body(olusan);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Aparatı günceller (tam güncelleme)")
    public EnvanterAparatiResponse guncelle(@PathVariable Long envanterId, @PathVariable Long id,
                                            @Valid @RequestBody UpdateEnvanterAparatiRequest istek) {
        return aparatService.guncelle(envanterId, id, istek);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Aparatı siler (soft delete)")
    public ResponseEntity<Void> sil(@PathVariable Long envanterId, @PathVariable Long id) {
        aparatService.sil(envanterId, id);
        return ResponseEntity.noContent().build();
    }
}
