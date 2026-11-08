package tr.depo.backend.model;

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
import tr.depo.backend.common.web.ApiYollari;
import tr.depo.backend.common.web.KaynakKonumu;
import tr.depo.backend.model.dto.CreateModelRequest;
import tr.depo.backend.model.dto.ModelResponse;
import tr.depo.backend.model.dto.UpdateModelRequest;

@RestController
@RequestMapping(ModelController.YOL)
@Tag(name = "Modeller", description = "Demirbaş ve sarf modelleri (tur ile ayrılır; marka ve kategori aynı türde)")
public class ModelController {

    static final String YOL = ApiYollari.V1 + "/modeller";

    private final ModelService modelService;

    public ModelController(ModelService modelService) {
        this.modelService = modelService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Modeli getirir", description = "Silinmiş model için 404.")
    public ModelResponse getir(@PathVariable Long id) {
        return modelService.getir(id);
    }

    @GetMapping
    @Operation(summary = "Modelleri listeler", description = "Sayfalı; sıralama alanları: id, tur, ad, kayitTarihi, olusturmaZamani.")
    public SayfaCevabi<ModelResponse> listele(@ParameterObject Pageable sayfa) {
        return modelService.listele(sayfa);
    }

    @PostMapping
    @Operation(summary = "Model oluşturur", description = "SARF modelinde markaId ve aktif zorunlu (eksikse 422).")
    public ResponseEntity<ModelResponse> olustur(@Valid @RequestBody CreateModelRequest istek) {
        ModelResponse olusan = modelService.olustur(istek);
        return ResponseEntity.created(KaynakKonumu.yeniKayit(olusan.id())).body(olusan);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modeli günceller (tam güncelleme)", description = "versiyon güncel değilse 409.")
    public ModelResponse guncelle(@PathVariable Long id, @Valid @RequestBody UpdateModelRequest istek) {
        return modelService.guncelle(id, istek);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Modeli siler (soft delete)", description = "Aktif envanter kullanıyorsa 409.")
    public ResponseEntity<Void> sil(@PathVariable Long id) {
        modelService.sil(id);
        return ResponseEntity.noContent().build();
    }
}
