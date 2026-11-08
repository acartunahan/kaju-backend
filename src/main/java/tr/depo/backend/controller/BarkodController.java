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
import tr.depo.backend.dto.BarkodResponse;
import tr.depo.backend.dto.CreateBarkodRequest;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.UpdateBarkodRequest;
import tr.depo.backend.service.BarkodService;
import tr.depo.backend.util.KaynakKonumu;

@RestController
@RequestMapping(BarkodController.YOL)
@Tag(name = "Envanter barkodları", description = "Demirbaş ya da sarf malzemesinin barkodları")
public class BarkodController {

    static final String YOL = ApiPaths.ENVANTER + "/barkodlar";

    private final BarkodService barkodService;

    public BarkodController(BarkodService barkodService) {
        this.barkodService = barkodService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Barkodu getirir")
    public BarkodResponse getir(@PathVariable Long envanterId, @PathVariable Long id) {
        return barkodService.getir(envanterId, id);
    }

    @GetMapping
    @Operation(summary = "Envanterin barkodlarını listeler", description = "Sıralama alanları: id, barkod, olusturmaZamani.")
    public SayfaResponse<BarkodResponse> listele(@PathVariable Long envanterId, @ParameterObject Pageable sayfa) {
        return barkodService.listele(envanterId, sayfa);
    }

    @PostMapping
    @Operation(summary = "Envantere barkod ekler")
    public ResponseEntity<BarkodResponse> olustur(@PathVariable Long envanterId,
                                                  @Valid @RequestBody CreateBarkodRequest istek) {
        BarkodResponse olusan = barkodService.olustur(envanterId, istek);
        return ResponseEntity.created(KaynakKonumu.yeniKayit(olusan.id())).body(olusan);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Barkodu günceller (tam güncelleme)")
    public BarkodResponse guncelle(@PathVariable Long envanterId, @PathVariable Long id,
                                   @Valid @RequestBody UpdateBarkodRequest istek) {
        return barkodService.guncelle(envanterId, id, istek);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Barkodu siler (soft delete)")
    public ResponseEntity<Void> sil(@PathVariable Long envanterId, @PathVariable Long id) {
        barkodService.sil(envanterId, id);
        return ResponseEntity.noContent().build();
    }
}
