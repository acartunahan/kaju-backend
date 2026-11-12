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
import tr.depo.backend.dto.CreateFirmaRequest;
import tr.depo.backend.dto.FirmaResponse;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.TurFiltreRequest;
import tr.depo.backend.dto.UpdateFirmaRequest;
import tr.depo.backend.service.FirmaService;
import tr.depo.backend.util.KaynakKonumu;

@RestController
@RequestMapping(FirmaController.YOL)
@Tag(name = "Firmalar", description = "Demirbaş ve sarf firmaları (tur ile ayrılır)")
public class FirmaController {

    static final String YOL = ApiPaths.V1 + "/firmalar";

    private final FirmaService firmaService;

    public FirmaController(FirmaService firmaService) {
        this.firmaService = firmaService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Firmayı getirir", description = "Silinmiş firma için 404.")
    public FirmaResponse getir(@PathVariable Long id) {
        return firmaService.getir(id);
    }

    @GetMapping
    @Operation(summary = "Firmaları listeler",
        description = "Sayfalı; sıralama alanları: id, tur, ad, kayitTarihi, olusturmaZamani. tur verilirse yalnızca o türden.")
    public SayfaResponse<FirmaResponse> listele(@ParameterObject TurFiltreRequest filtre, @ParameterObject Pageable sayfa) {
        return firmaService.listele(filtre, sayfa);
    }

    @PostMapping
    @Operation(summary = "Firma oluşturur",
        description = "DEMIRBAS: durumKodu ve kayitTarihi zorunlu; SARF: ad zorunlu (eksikse 422).")
    public ResponseEntity<FirmaResponse> olustur(@Valid @RequestBody CreateFirmaRequest istek) {
        FirmaResponse olusan = firmaService.olustur(istek);
        return ResponseEntity.created(KaynakKonumu.yeniKayit(olusan.id())).body(olusan);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Firmayı günceller (tam güncelleme)", description = "versiyon güncel değilse 409.")
    public FirmaResponse guncelle(@PathVariable Long id, @Valid @RequestBody UpdateFirmaRequest istek) {
        return firmaService.guncelle(id, istek);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Firmayı siler (soft delete)", description = "Aktif ihale kullanıyorsa 409.")
    public ResponseEntity<Void> sil(@PathVariable Long id) {
        firmaService.sil(id);
        return ResponseEntity.noContent().build();
    }
}
