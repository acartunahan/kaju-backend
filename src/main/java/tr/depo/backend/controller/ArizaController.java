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
import tr.depo.backend.dto.ArizaResponse;
import tr.depo.backend.dto.CreateArizaRequest;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.UpdateArizaRequest;
import tr.depo.backend.service.ArizaService;
import tr.depo.backend.util.KaynakKonumu;

@RestController
@RequestMapping(ArizaController.YOL)
@Tag(name = "Arızalar", description = "Arıza kayıtları; işlem geçmişi /arizalar/{arizaId}/islemler altında")
public class ArizaController {

    public static final String YOL = ApiPaths.V1 + "/arizalar";

    private final ArizaService arizaService;

    public ArizaController(ArizaService arizaService) {
        this.arizaService = arizaService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Arızayı getirir", description = "Silinmiş arıza için 404.")
    public ArizaResponse getir(@PathVariable Long id) {
        return arizaService.getir(id);
    }

    @GetMapping
    @Operation(summary = "Arızaları listeler", description = "Sayfalı; sıralama alanları: id, tarih, miad, olusturmaZamani.")
    public SayfaResponse<ArizaResponse> listele(@ParameterObject Pageable sayfa) {
        return arizaService.listele(sayfa);
    }

    @PostMapping
    @Operation(summary = "Arıza kaydı oluşturur")
    public ResponseEntity<ArizaResponse> olustur(@Valid @RequestBody CreateArizaRequest istek) {
        ArizaResponse olusan = arizaService.olustur(istek);
        return ResponseEntity.created(KaynakKonumu.yeniKayit(olusan.id())).body(olusan);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Arızayı günceller (tam güncelleme)", description = "versiyon güncel değilse 409.")
    public ArizaResponse guncelle(@PathVariable Long id, @Valid @RequestBody UpdateArizaRequest istek) {
        return arizaService.guncelle(id, istek);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Arızayı siler (soft delete)", description = "Arıza işlemleri de silinir.")
    public ResponseEntity<Void> sil(@PathVariable Long id) {
        arizaService.sil(id);
        return ResponseEntity.noContent().build();
    }
}
