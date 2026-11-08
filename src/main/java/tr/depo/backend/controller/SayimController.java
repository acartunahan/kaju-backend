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
import tr.depo.backend.dto.CreateSayimRequest;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.SayimResponse;
import tr.depo.backend.dto.UpdateSayimRequest;
import tr.depo.backend.service.SayimService;
import tr.depo.backend.util.KaynakKonumu;

@RestController
@RequestMapping(SayimController.YOL)
@Tag(name = "Sayımlar", description = "Sayım başlıkları; satırlar /sayimlar/{sayimId}/satirlar altında")
public class SayimController {

    public static final String YOL = ApiPaths.V1 + "/sayimlar";

    private final SayimService sayimService;

    public SayimController(SayimService sayimService) {
        this.sayimService = sayimService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Sayımı getirir", description = "Silinmiş sayım için 404.")
    public SayimResponse getir(@PathVariable Long id) {
        return sayimService.getir(id);
    }

    @GetMapping
    @Operation(summary = "Sayımları listeler", description = "Sayfalı; sıralama alanları: id, baslik, baslangicTarihi, olusturmaZamani.")
    public SayfaResponse<SayimResponse> listele(@ParameterObject Pageable sayfa) {
        return sayimService.listele(sayfa);
    }

    @PostMapping
    @Operation(summary = "Sayım oluşturur")
    public ResponseEntity<SayimResponse> olustur(@Valid @RequestBody CreateSayimRequest istek) {
        SayimResponse olusan = sayimService.olustur(istek);
        return ResponseEntity.created(KaynakKonumu.yeniKayit(olusan.id())).body(olusan);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Sayımı günceller (tam güncelleme)", description = "versiyon güncel değilse 409.")
    public SayimResponse guncelle(@PathVariable Long id, @Valid @RequestBody UpdateSayimRequest istek) {
        return sayimService.guncelle(id, istek);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Sayımı siler (soft delete)", description = "Sayım satırları da silinir.")
    public ResponseEntity<Void> sil(@PathVariable Long id) {
        sayimService.sil(id);
        return ResponseEntity.noContent().build();
    }
}
