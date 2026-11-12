package tr.depo.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tr.depo.backend.dto.ArizaResponse;
import tr.depo.backend.dto.CreateDemirbasRequest;
import tr.depo.backend.dto.DemirbasFiltreRequest;
import tr.depo.backend.dto.DemirbasResponse;
import tr.depo.backend.dto.EnvanterHareketiResponse;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.UpdateDemirbasRequest;
import tr.depo.backend.service.ArizaService;
import tr.depo.backend.service.DemirbasService;
import tr.depo.backend.service.EnvanterHareketiService;
import tr.depo.backend.util.KaynakKonumu;

@RestController
@RequestMapping(DemirbasController.YOL)
@Tag(name = "Demirbaşlar", description = "Tek tek izlenen varlıklar (ENVANTER, TUR = DEMIRBAS)")
public class DemirbasController {

    static final String YOL = ApiPaths.V1 + "/demirbaslar";

    private final DemirbasService demirbasService;
    private final EnvanterHareketiService hareketService;
    private final ArizaService arizaService;

    public DemirbasController(DemirbasService demirbasService, EnvanterHareketiService hareketService,
                              ArizaService arizaService) {
        this.demirbasService = demirbasService;
        this.hareketService = hareketService;
        this.arizaService = arizaService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Demirbaşı getirir", description = "Silinmiş ya da sarf olan kayıt için 404.")
    public DemirbasResponse getir(@PathVariable Long id) {
        return demirbasService.getir(id);
    }

    @GetMapping
    @Operation(summary = "Demirbaşları listeler",
        description = "Sayfalı; sıralama alanları: id, demirbasNo, seriNo, ad, kayitTarihi, olusturmaZamani. Filtreler "
            + "birlikte uygulanır: kategoriId (alt kategoriler dahil; altKategoriler=false ile yalnızca o kategori), "
            + "markaId, modelId, depoId, ihaleId, durum, zimmetliSicil (tam eşleşme; boşsa 400), zimmetli (true: birine "
            + "zimmetli, false: kimseye zimmetli olmayan), q (ad, seri no, demirbaş no, açıklama, barkod).")
    public SayfaResponse<DemirbasResponse> listele(@Valid @ParameterObject DemirbasFiltreRequest filtre,
                                                   @ParameterObject Pageable sayfa) {
        return demirbasService.listele(filtre, sayfa);
    }

    @GetMapping("/bul")
    @Operation(summary = "Barkod, seri no ya da demirbaş no ile bulur",
        description = "Tam eşleşme; üçünden tam biri verilmeli (aksi halde 400). Legacy'de tekrarlı olabildiği için "
            + "liste döner (en fazla 100); bulunamazsa boş liste.")
    public List<DemirbasResponse> bul(@RequestParam(required = false) String barkod,
                                      @RequestParam(required = false) String seriNo,
                                      @RequestParam(required = false) Long demirbasNo) {
        return demirbasService.bul(barkod, seriNo, demirbasNo);
    }

    @GetMapping("/{id}/hareketler")
    @Operation(summary = "Demirbaşın hareketlerini listeler",
        description = "Sayfalı; varsayılan sıra en yeni önce; sıralama alanları: id, kayitTarihi, olusturmaZamani. "
            + "Demirbaş yoksa, silinmişse ya da sarf ise 404.")
    public SayfaResponse<EnvanterHareketiResponse> hareketler(@PathVariable Long id, @ParameterObject Pageable sayfa) {
        return hareketService.demirbasHareketleri(id, sayfa);
    }

    @GetMapping("/{id}/arizalar")
    @Operation(summary = "Demirbaşın arızalarını listeler",
        description = "Sayfalı; varsayılan sıra en yeni önce; sıralama alanları: id, tarih, miad, olusturmaZamani. "
            + "Demirbaş yoksa, silinmişse ya da sarf ise 404.")
    public SayfaResponse<ArizaResponse> arizalar(@PathVariable Long id, @ParameterObject Pageable sayfa) {
        return arizaService.demirbasArizalari(id, sayfa);
    }

    @PostMapping
    @Operation(summary = "Demirbaş oluşturur",
        description = "Kategori, marka ve model zorunlu (eksikse 400), DEMIRBAS türünde olmalı ve model seçilen markaya "
            + "ait olmalı; garanti bitişi başlangıçtan önce olamaz (422).")
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
