package tr.depo.backend.dto;

import java.util.Objects;
import tr.depo.domain.ortak.BaseEntity;

/**
 * "Bu kayda başka bir entity referans veriyor" bilgisi; silmeden önce aktif referans var mı diye bakılır.
 *
 * @param kaynak     referans veren entity sınıfı (ör. {@code Envanter.class})
 * @param alan       referans veren entity'deki ilişki alanı (JPQL yolu, ör. {@code "marka"})
 * @param kaynakAdi  hata mesajında görünecek ad (ör. {@code "envanter"})
 */
public record KullanimReferansi(Class<? extends BaseEntity> kaynak, String alan, String kaynakAdi) {

    public KullanimReferansi {
        Objects.requireNonNull(kaynak, "kaynak");
        Objects.requireNonNull(alan, "alan");
        Objects.requireNonNull(kaynakAdi, "kaynakAdi");
    }
}
