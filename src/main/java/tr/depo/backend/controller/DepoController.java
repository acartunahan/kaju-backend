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
import tr.depo.backend.dto.CreateDepoRequest;
import tr.depo.backend.dto.DepoResponse;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.UpdateDepoRequest;
import tr.depo.backend.service.DepoService;
import tr.depo.backend.util.KaynakKonumu;

@RestController
@RequestMapping(DepoController.YOL)
@Tag(name = "Depolar")
public class DepoController {

    static final String YOL = ApiPaths.V1 + "/depolar";

    private final DepoService depoService;

    public DepoController(DepoService depoService) {
        this.depoService = depoService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Depoyu getirir", description = "Silinmiş depo için 404.")
    public DepoResponse getir(@PathVariable Long id) {
        return depoService.getir(id);
    }

    @GetMapping
    @Operation(summary = "Depoları listeler", description = "Sayfalı; sıralama alanları: id, ad, kayitTarihi, olusturmaZamani.")
    public SayfaResponse<DepoResponse> listele(@ParameterObject Pageable sayfa) {
        return depoService.listele(sayfa);
    }

    @PostMapping
    @Operation(summary = "Depo oluşturur")
    public ResponseEntity<DepoResponse> olustur(@Valid @RequestBody CreateDepoRequest istek) {
        DepoResponse olusan = depoService.olustur(istek);
        return ResponseEntity.created(KaynakKonumu.yeniKayit(olusan.id())).body(olusan);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Depoyu günceller (tam güncelleme)", description = "versiyon güncel değilse 409.")
    public DepoResponse guncelle(@PathVariable Long id, @Valid @RequestBody UpdateDepoRequest istek) {
        return depoService.guncelle(id, istek);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Depoyu siler (soft delete)", description = "Depoya bağlı aktif kayıt varsa 409.")
    public ResponseEntity<Void> sil(@PathVariable Long id) {
        depoService.sil(id);
        return ResponseEntity.noContent().build();
    }
}
