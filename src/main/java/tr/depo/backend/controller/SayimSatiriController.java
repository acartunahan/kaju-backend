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
import tr.depo.backend.dto.CreateSayimSatiriRequest;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.SayimSatiriResponse;
import tr.depo.backend.dto.UpdateSayimSatiriRequest;
import tr.depo.backend.service.SayimSatiriService;
import tr.depo.backend.util.KaynakKonumu;

@RestController
@RequestMapping(SayimSatiriController.YOL)
@Tag(name = "Sayım satırları", description = "Yerel ve genel sayım satırları (tur ile ayrılır)")
public class SayimSatiriController {

    static final String YOL = SayimController.YOL + "/{sayimId}/satirlar";

    private final SayimSatiriService satirService;

    public SayimSatiriController(SayimSatiriService satirService) {
        this.satirService = satirService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Sayım satırını getirir")
    public SayimSatiriResponse getir(@PathVariable Long sayimId, @PathVariable Long id) {
        return satirService.getir(sayimId, id);
    }

    @GetMapping
    @Operation(summary = "Sayımın satırlarını listeler", description = "Sıralama alanları: id, islemTarihi, olusturmaZamani.")
    public SayfaResponse<SayimSatiriResponse> listele(@PathVariable Long sayimId, @ParameterObject Pageable sayfa) {
        return satirService.listele(sayimId, sayfa);
    }

    @PostMapping
    @Operation(summary = "Sayıma satır ekler", description = "Türe göre zorunlu alanlar ve işlem değerleri farklıdır (422).")
    public ResponseEntity<SayimSatiriResponse> olustur(@PathVariable Long sayimId,
                                                       @Valid @RequestBody CreateSayimSatiriRequest istek) {
        SayimSatiriResponse olusan = satirService.olustur(sayimId, istek);
        return ResponseEntity.created(KaynakKonumu.yeniKayit(olusan.id())).body(olusan);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Sayım satırını günceller (tam güncelleme)", description = "Satır türü değiştirilemez.")
    public SayimSatiriResponse guncelle(@PathVariable Long sayimId, @PathVariable Long id,
                                        @Valid @RequestBody UpdateSayimSatiriRequest istek) {
        return satirService.guncelle(sayimId, id, istek);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Sayım satırını siler (soft delete)")
    public ResponseEntity<Void> sil(@PathVariable Long sayimId, @PathVariable Long id) {
        satirService.sil(sayimId, id);
        return ResponseEntity.noContent().build();
    }
}
