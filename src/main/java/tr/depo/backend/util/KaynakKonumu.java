package tr.depo.backend.util;

import java.net.URI;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** POST sonrası {@code Location} başlığı: geçerli istek yoluna yeni kaydın ID'si eklenir ({@code /api/v1/x/{id}}). */
public final class KaynakKonumu {

    private KaynakKonumu() {
    }

    public static URI yeniKayit(Long id) {
        return ServletUriComponentsBuilder.fromCurrentRequestUri().path("/{id}").buildAndExpand(id).toUri();
    }
}
