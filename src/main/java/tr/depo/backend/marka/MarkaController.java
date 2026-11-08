package tr.depo.backend.marka;

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
import tr.depo.backend.marka.dto.CreateMarkaRequest;
import tr.depo.backend.marka.dto.MarkaResponse;
import tr.depo.backend.marka.dto.UpdateMarkaRequest;

@RestController
@RequestMapping(MarkaController.YOL)
@Tag(name = "Markalar", description = "Demirbaş ve sarf markaları (tur ile ayrılır)")
public class MarkaController {

    static final String YOL = ApiYollari.V1 + "/markalar";

    private final MarkaService markaService;

    public MarkaController(MarkaService markaService) {
        this.markaService = markaService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Markayı getirir", description = "Silinmiş marka için 404.")
    public MarkaResponse getir(@PathVariable Long id) {
        return markaService.getir(id);
    }

    @GetMapping
    @Operation(summary = "Markaları listeler",
        description = "Sayfalı; sıralama alanları: id, tur, ad, kayitTarihi, olusturmaZamani. Silinmişler dönmez.")
    public SayfaCevabi<MarkaResponse> listele(@ParameterObject Pageable sayfa) {
        return markaService.listele(sayfa);
    }

    @PostMapping
    @Operation(summary = "Marka oluşturur")
    public ResponseEntity<MarkaResponse> olustur(@Valid @RequestBody CreateMarkaRequest istek) {
        MarkaResponse olusan = markaService.olustur(istek);
        return ResponseEntity.created(KaynakKonumu.yeniKayit(olusan.id())).body(olusan);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Markayı günceller (tam güncelleme)", description = "versiyon güncel değilse 409.")
    public MarkaResponse guncelle(@PathVariable Long id, @Valid @RequestBody UpdateMarkaRequest istek) {
        return markaService.guncelle(id, istek);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Markayı siler (soft delete)", description = "Aktif model ya da envanter kullanıyorsa 409.")
    public ResponseEntity<Void> sil(@PathVariable Long id) {
        markaService.sil(id);
        return ResponseEntity.noContent().build();
    }
}
