package tr.depo.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tr.depo.backend.dto.CreateEnvanterHareketiRequest;
import tr.depo.backend.dto.EnvanterHareketiResponse;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.service.EnvanterHareketiService;
import tr.depo.backend.util.KaynakKonumu;

/** Geçmiş kaydı: yalnızca GET ve POST. PUT/DELETE bu kaynakta yoktur (405). */
@RestController
@RequestMapping(EnvanterHareketiController.YOL)
@Tag(name = "Envanter hareketleri", description = "Demirbaş transferleri ve sarf stok hareketleri (değiştirilemez geçmiş kaydı)")
public class EnvanterHareketiController {

    static final String YOL = ApiPaths.V1 + "/envanter-hareketleri";

    private final EnvanterHareketiService hareketService;

    public EnvanterHareketiController(EnvanterHareketiService hareketService) {
        this.hareketService = hareketService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Hareketi getirir")
    public EnvanterHareketiResponse getir(@PathVariable Long id) {
        return hareketService.getir(id);
    }

    @GetMapping
    @Operation(summary = "Hareketleri listeler", description = "Sayfalı; sıralama alanları: id, kayitTarihi, olusturmaZamani.")
    public SayfaResponse<EnvanterHareketiResponse> listele(@ParameterObject Pageable sayfa) {
        return hareketService.listele(sayfa);
    }

    @PostMapping
    @Operation(summary = "Hareket kaydeder",
        description = "Envanter hareketle aynı türde olmalı; demirbaş hareketinde hareketNoKodu zorunlu (422).")
    public ResponseEntity<EnvanterHareketiResponse> olustur(@Valid @RequestBody CreateEnvanterHareketiRequest istek) {
        EnvanterHareketiResponse olusan = hareketService.olustur(istek);
        return ResponseEntity.created(KaynakKonumu.yeniKayit(olusan.id())).body(olusan);
    }
}
