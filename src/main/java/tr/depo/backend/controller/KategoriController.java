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
import tr.depo.backend.dto.CreateKategoriRequest;
import tr.depo.backend.dto.KategoriResponse;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.UpdateKategoriRequest;
import tr.depo.backend.service.KategoriService;
import tr.depo.backend.util.KaynakKonumu;

@RestController
@RequestMapping(KategoriController.YOL)
@Tag(name = "Kategoriler", description = "Katalog kategori ağacı (tur ile ayrılır; üst kategori aynı türde)")
public class KategoriController {

    static final String YOL = ApiPaths.V1 + "/kategoriler";

    private final KategoriService kategoriService;

    public KategoriController(KategoriService kategoriService) {
        this.kategoriService = kategoriService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Kategoriyi getirir", description = "Silinmiş kategori için 404.")
    public KategoriResponse getir(@PathVariable Long id) {
        return kategoriService.getir(id);
    }

    @GetMapping
    @Operation(summary = "Kategorileri listeler", description = "Sayfalı; sıralama alanları: id, tur, ad, kayitTarihi, olusturmaZamani.")
    public SayfaResponse<KategoriResponse> listele(@ParameterObject Pageable sayfa) {
        return kategoriService.listele(sayfa);
    }

    @PostMapping
    @Operation(summary = "Kategori oluşturur", description = "DEMIRBAS kategorisinde durumKodu zorunlu (eksikse 422).")
    public ResponseEntity<KategoriResponse> olustur(@Valid @RequestBody CreateKategoriRequest istek) {
        KategoriResponse olusan = kategoriService.olustur(istek);
        return ResponseEntity.created(KaynakKonumu.yeniKayit(olusan.id())).body(olusan);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Kategoriyi günceller (tam güncelleme)",
        description = "versiyon güncel değilse 409; üst kategori değişikliği döngü oluşturursa 422.")
    public KategoriResponse guncelle(@PathVariable Long id, @Valid @RequestBody UpdateKategoriRequest istek) {
        return kategoriService.guncelle(id, istek);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Kategoriyi siler (soft delete)", description = "Aktif alt kategori, model ya da envanter varsa 409.")
    public ResponseEntity<Void> sil(@PathVariable Long id) {
        kategoriService.sil(id);
        return ResponseEntity.noContent().build();
    }
}
