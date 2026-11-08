package tr.depo.backend.ihale;

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
import tr.depo.backend.ihale.dto.CreateIhaleRequest;
import tr.depo.backend.ihale.dto.IhaleResponse;
import tr.depo.backend.ihale.dto.UpdateIhaleRequest;

@RestController
@RequestMapping(IhaleController.YOL)
@Tag(name = "İhaleler", description = "İhaleler (yalnızca demirbaş firmasına bağlanır)")
public class IhaleController {

    static final String YOL = ApiYollari.V1 + "/ihaleler";

    private final IhaleService ihaleService;

    public IhaleController(IhaleService ihaleService) {
        this.ihaleService = ihaleService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "İhaleyi getirir", description = "Silinmiş ihale için 404.")
    public IhaleResponse getir(@PathVariable Long id) {
        return ihaleService.getir(id);
    }

    @GetMapping
    @Operation(summary = "İhaleleri listeler", description = "Sayfalı; sıralama alanları: id, ad, ihaleTarihi, kayitTarihi, olusturmaZamani.")
    public SayfaCevabi<IhaleResponse> listele(@ParameterObject Pageable sayfa) {
        return ihaleService.listele(sayfa);
    }

    @PostMapping
    @Operation(summary = "İhale oluşturur", description = "firmaId verilirse DEMIRBAS türünde aktif bir firma olmalı (aksi halde 422).")
    public ResponseEntity<IhaleResponse> olustur(@Valid @RequestBody CreateIhaleRequest istek) {
        IhaleResponse olusan = ihaleService.olustur(istek);
        return ResponseEntity.created(KaynakKonumu.yeniKayit(olusan.id())).body(olusan);
    }

    @PutMapping("/{id}")
    @Operation(summary = "İhaleyi günceller (tam güncelleme)", description = "versiyon güncel değilse 409.")
    public IhaleResponse guncelle(@PathVariable Long id, @Valid @RequestBody UpdateIhaleRequest istek) {
        return ihaleService.guncelle(id, istek);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "İhaleyi siler (soft delete)", description = "Aktif envanter kullanıyorsa 409.")
    public ResponseEntity<Void> sil(@PathVariable Long id) {
        ihaleService.sil(id);
        return ResponseEntity.noContent().build();
    }
}
