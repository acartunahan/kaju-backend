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
import tr.depo.backend.dto.CreateDemirbasRequest;
import tr.depo.backend.dto.DemirbasResponse;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.UpdateDemirbasRequest;
import tr.depo.backend.service.DemirbasService;
import tr.depo.backend.util.KaynakKonumu;

@RestController
@RequestMapping(DemirbasController.YOL)
@Tag(name = "Demirbaşlar", description = "Tek tek izlenen varlıklar (ENVANTER, TUR = DEMIRBAS)")
public class DemirbasController {

    static final String YOL = ApiPaths.V1 + "/demirbaslar";

    private final DemirbasService demirbasService;

    public DemirbasController(DemirbasService demirbasService) {
        this.demirbasService = demirbasService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Demirbaşı getirir", description = "Silinmiş ya da sarf olan kayıt için 404.")
    public DemirbasResponse getir(@PathVariable Long id) {
        return demirbasService.getir(id);
    }

    @GetMapping
    @Operation(summary = "Demirbaşları listeler",
        description = "Sayfalı; sıralama alanları: id, demirbasNo, seriNo, ad, kayitTarihi, olusturmaZamani.")
    public SayfaResponse<DemirbasResponse> listele(@ParameterObject Pageable sayfa) {
        return demirbasService.listele(sayfa);
    }

    @PostMapping
    @Operation(summary = "Demirbaş oluşturur",
        description = "Kategori, marka ve model DEMIRBAS türünde olmalı; garanti bitişi başlangıçtan önce olamaz (422).")
    public ResponseEntity<DemirbasResponse> olustur(@Valid @RequestBody CreateDemirbasRequest istek) {
        DemirbasResponse olusan = demirbasService.olustur(istek);
        return ResponseEntity.created(KaynakKonumu.yeniKayit(olusan.id())).body(olusan);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Demirbaşı günceller (tam güncelleme)", description = "versiyon güncel değilse 409.")
    public DemirbasResponse guncelle(@PathVariable Long id, @Valid @RequestBody UpdateDemirbasRequest istek) {
        return demirbasService.guncelle(id, istek);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Demirbaşı siler (soft delete)",
        description = "Barkod, not, aparat ve fotoğrafları da silinir; hareket, arıza ve sayım geçmişi kalır.")
    public ResponseEntity<Void> sil(@PathVariable Long id) {
        demirbasService.sil(id);
        return ResponseEntity.noContent().build();
    }
}
