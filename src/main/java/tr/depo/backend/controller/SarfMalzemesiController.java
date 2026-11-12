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
import tr.depo.backend.dto.CreateSarfMalzemesiRequest;
import tr.depo.backend.dto.EnvanterHareketiResponse;
import tr.depo.backend.dto.SarfMalzemesiFiltreRequest;
import tr.depo.backend.dto.SarfMalzemesiResponse;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.UpdateSarfMalzemesiRequest;
import tr.depo.backend.service.EnvanterHareketiService;
import tr.depo.backend.service.SarfMalzemesiService;
import tr.depo.backend.util.KaynakKonumu;

@RestController
@RequestMapping(SarfMalzemesiController.YOL)
@Tag(name = "Sarf malzemeleri", description = "Tüketim malzemeleri (ENVANTER, TUR = SARF)")
public class SarfMalzemesiController {

    static final String YOL = ApiPaths.V1 + "/sarf-malzemeleri";

    private final SarfMalzemesiService sarfMalzemesiService;
    private final EnvanterHareketiService hareketService;

    public SarfMalzemesiController(SarfMalzemesiService sarfMalzemesiService, EnvanterHareketiService hareketService) {
        this.sarfMalzemesiService = sarfMalzemesiService;
        this.hareketService = hareketService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Sarf malzemesini getirir", description = "Silinmiş ya da demirbaş olan kayıt için 404.")
    public SarfMalzemesiResponse getir(@PathVariable Long id) {
        return sarfMalzemesiService.getir(id);
    }

    @GetMapping
    @Operation(summary = "Sarf malzemelerini listeler",
        description = "Sayfalı; sıralama alanları: id, ad, kayitTarihi, olusturmaZamani. Filtreler birlikte uygulanır: "
            + "kategoriId (alt kategoriler dahil; altKategoriler=false ile yalnızca o kategori), markaId, modelId, depoId, "
            + "q (ad, açıklama, barkod).")
    public SayfaResponse<SarfMalzemesiResponse> listele(@Valid @ParameterObject SarfMalzemesiFiltreRequest filtre,
                                                        @ParameterObject Pageable sayfa) {
        return sarfMalzemesiService.listele(filtre, sayfa);
    }

    @GetMapping("/{id}/hareketler")
    @Operation(summary = "Sarf malzemesinin stok hareketlerini listeler",
        description = "Sayfalı; varsayılan sıra en yeni önce; sıralama alanları: id, kayitTarihi, olusturmaZamani. "
            + "Sarf malzemesi yoksa, silinmişse ya da demirbaş ise 404.")
    public SayfaResponse<EnvanterHareketiResponse> hareketler(@PathVariable Long id, @ParameterObject Pageable sayfa) {
        return hareketService.sarfHareketleri(id, sayfa);
    }

    @PostMapping
    @Operation(summary = "Sarf malzemesi oluşturur", description = "Kategori, marka ve model zorunlu (eksikse 400), SARF türünde olmalı ve model seçilen markaya ait olmalı (422).")
    public ResponseEntity<SarfMalzemesiResponse> olustur(@Valid @RequestBody CreateSarfMalzemesiRequest istek) {
        SarfMalzemesiResponse olusan = sarfMalzemesiService.olustur(istek);
        return ResponseEntity.created(KaynakKonumu.yeniKayit(olusan.id())).body(olusan);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Sarf malzemesini günceller (tam güncelleme)", description = "versiyon güncel değilse 409.")
    public SarfMalzemesiResponse guncelle(@PathVariable Long id, @Valid @RequestBody UpdateSarfMalzemesiRequest istek) {
        return sarfMalzemesiService.guncelle(id, istek);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Sarf malzemesini siler (soft delete)", description = "Alt kayıtları da silinir; stok hareketleri kalır.")
    public ResponseEntity<Void> sil(@PathVariable Long id) {
        sarfMalzemesiService.sil(id);
        return ResponseEntity.noContent().build();
    }
}
