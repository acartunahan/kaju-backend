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
import tr.depo.backend.dto.ArizaIslemiResponse;
import tr.depo.backend.dto.CreateArizaIslemiRequest;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.UpdateArizaIslemiRequest;
import tr.depo.backend.service.ArizaIslemiService;
import tr.depo.backend.util.KaynakKonumu;

@RestController
@RequestMapping(ArizaIslemiController.YOL)
@Tag(name = "Arıza işlemleri", description = "Arızanın işlem geçmişi (işlem, bilgi, şikayet, teşekkür)")
public class ArizaIslemiController {

    static final String YOL = ArizaController.YOL + "/{arizaId}/islemler";

    private final ArizaIslemiService islemService;

    public ArizaIslemiController(ArizaIslemiService islemService) {
        this.islemService = islemService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Arıza işlemini getirir")
    public ArizaIslemiResponse getir(@PathVariable Long arizaId, @PathVariable Long id) {
        return islemService.getir(arizaId, id);
    }

    @GetMapping
    @Operation(summary = "Arızanın işlemlerini listeler", description = "Sıralama alanları: id, zaman, siraNo, olusturmaZamani.")
    public SayfaResponse<ArizaIslemiResponse> listele(@PathVariable Long arizaId, @ParameterObject Pageable sayfa) {
        return islemService.listele(arizaId, sayfa);
    }

    @PostMapping
    @Operation(summary = "Arızaya işlem ekler")
    public ResponseEntity<ArizaIslemiResponse> olustur(@PathVariable Long arizaId,
                                                       @Valid @RequestBody CreateArizaIslemiRequest istek) {
        ArizaIslemiResponse olusan = islemService.olustur(arizaId, istek);
        return ResponseEntity.created(KaynakKonumu.yeniKayit(olusan.id())).body(olusan);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Arıza işlemini günceller (tam güncelleme)")
    public ArizaIslemiResponse guncelle(@PathVariable Long arizaId, @PathVariable Long id,
                                        @Valid @RequestBody UpdateArizaIslemiRequest istek) {
        return islemService.guncelle(arizaId, id, istek);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Arıza işlemini siler (soft delete)")
    public ResponseEntity<Void> sil(@PathVariable Long arizaId, @PathVariable Long id) {
        islemService.sil(arizaId, id);
        return ResponseEntity.noContent().build();
    }
}
