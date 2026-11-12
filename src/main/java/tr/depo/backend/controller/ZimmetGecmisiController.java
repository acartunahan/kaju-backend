package tr.depo.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tr.depo.backend.dto.SayfaResponse;
import tr.depo.backend.dto.ZimmetGecmisiResponse;
import tr.depo.backend.service.ZimmetGecmisiService;

@RestController
@RequestMapping(ZimmetGecmisiController.YOL)
@Tag(name = "Zimmet geçmişi", description = "Demirbaşın zimmetli sicil değişiklikleri (değiştirilemez geçmiş kaydı)")
public class ZimmetGecmisiController {

    static final String YOL = ApiPaths.V1 + "/demirbaslar/{demirbasId}/zimmet-gecmisi";

    private final ZimmetGecmisiService zimmetGecmisiService;

    public ZimmetGecmisiController(ZimmetGecmisiService zimmetGecmisiService) {
        this.zimmetGecmisiService = zimmetGecmisiService;
    }

    @GetMapping
    @Operation(summary = "Demirbaşın hangi sicillerde bulunduğunu listeler",
        description = "Sayfalı; varsayılan sıra en yeni değişiklik önce; sıralama alanları: id, degisimZamani. "
            + "Kayıtlar migration anından (MIGRATION_BASLANGICI) itibaren tutulur; daha eskisi legacy'de yok. "
            + "Demirbaş yoksa, silinmişse ya da sarf ise 404.")
    public SayfaResponse<ZimmetGecmisiResponse> listele(@PathVariable Long demirbasId, @ParameterObject Pageable sayfa) {
        return zimmetGecmisiService.listele(demirbasId, sayfa);
    }
}
