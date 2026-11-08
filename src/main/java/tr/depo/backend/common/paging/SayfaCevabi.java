package tr.depo.backend.common.paging;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * Sayfalı liste cevabı. Spring {@code Page} nesnesi dışarı açılmaz: JSON biçimi Spring Data sürümüne bağlı
 * kalmasın ve gereksiz iç alanlar (pageable, sort ayrıntıları) sızmasın.
 *
 * @param icerik       bu sayfadaki kayıtlar
 * @param sayfa        sayfa numarası (0'dan başlar)
 * @param boyut        istenen sayfa boyutu (üst sınıra göre kırpılmış)
 * @param toplamEleman tüm sayfalardaki kayıt sayısı
 * @param toplamSayfa  toplam sayfa sayısı
 */
public record SayfaCevabi<T>(List<T> icerik, int sayfa, int boyut, long toplamEleman, int toplamSayfa) {

    public SayfaCevabi {
        icerik = List.copyOf(icerik);
    }

    public static <T> SayfaCevabi<T> of(Page<T> sayfa) {
        return new SayfaCevabi<>(sayfa.getContent(), sayfa.getNumber(), sayfa.getSize(), sayfa.getTotalElements(),
            sayfa.getTotalPages());
    }
}
