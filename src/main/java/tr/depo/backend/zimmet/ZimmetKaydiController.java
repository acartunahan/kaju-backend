package tr.depo.backend.zimmet;

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
import tr.depo.backend.common.paging.SayfaCevabi;
import tr.depo.backend.common.web.ApiYollari;
import tr.depo.backend.common.web.KaynakKonumu;
import tr.depo.backend.zimmet.dto.CreateZimmetKaydiRequest;
import tr.depo.backend.zimmet.dto.ZimmetKaydiResponse;

/** Geçmiş kaydı: yalnızca GET ve POST. PUT/DELETE bu kaynakta yoktur (405). */
@RestController
@RequestMapping(ZimmetKaydiController.YOL)
@Tag(name = "Zimmet kayıtları", description = "Sarf zimmet ve düşüm günlüğü (değiştirilemez geçmiş kaydı)")
public class ZimmetKaydiController {

    static final String YOL = ApiYollari.V1 + "/zimmet-kayitlari";

    private final ZimmetKaydiService zimmetKaydiService;

    public ZimmetKaydiController(ZimmetKaydiService zimmetKaydiService) {
        this.zimmetKaydiService = zimmetKaydiService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Zimmet kaydını getirir")
    public ZimmetKaydiResponse getir(@PathVariable Long id) {
        return zimmetKaydiService.getir(id);
    }

    @GetMapping
    @Operation(summary = "Zimmet kayıtlarını listeler", description = "Sayfalı; sıralama alanları: id, tarih, olusturmaZamani.")
    public SayfaCevabi<ZimmetKaydiResponse> listele(@ParameterObject Pageable sayfa) {
        return zimmetKaydiService.listele(sayfa);
    }

    @PostMapping
    @Operation(summary = "Zimmet ya da düşüm kaydeder")
    public ResponseEntity<ZimmetKaydiResponse> olustur(@Valid @RequestBody CreateZimmetKaydiRequest istek) {
        ZimmetKaydiResponse olusan = zimmetKaydiService.olustur(istek);
        return ResponseEntity.created(KaynakKonumu.yeniKayit(olusan.id())).body(olusan);
    }
}
