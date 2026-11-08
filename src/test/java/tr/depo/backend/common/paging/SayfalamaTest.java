package tr.depo.backend.common.paging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import tr.depo.backend.common.exception.GecersizIstekException;

class SayfalamaTest {

    @Test
    void shouldMapSpringPageToOwnResponseModel() {
        var sayfa = new PageImpl<>(List.of("a", "b"), PageRequest.of(1, 2), 5);

        SayfaCevabi<String> cevap = SayfaCevabi.of(sayfa);

        assertThat(cevap.icerik()).containsExactly("a", "b");
        assertThat(cevap.sayfa()).isEqualTo(1);
        assertThat(cevap.boyut()).isEqualTo(2);
        assertThat(cevap.toplamEleman()).isEqualTo(5);
        assertThat(cevap.toplamSayfa()).isEqualTo(3);
    }

    @Test
    void shouldAcceptAllowedSortFieldsAndRejectOthers() {
        Set<String> izinli = Set.of("ad", "id");

        assertThat(SiralamaDogrulayici.dogrula(PageRequest.of(0, 20, Sort.by("ad")), izinli)).isNotNull();
        assertThat(SiralamaDogrulayici.dogrula(PageRequest.of(0, 20), izinli)).isNotNull();
        assertThatThrownBy(() -> SiralamaDogrulayici.dogrula(PageRequest.of(0, 20, Sort.by("ad", "depo.ad")), izinli))
            .isInstanceOf(GecersizIstekException.class)
            .hasMessageContaining("depo.ad");
    }
}
