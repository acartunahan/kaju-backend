package tr.depo.backend.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
import java.io.UnsupportedEncodingException;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/** Entegrasyon testlerinde bir kaynağın HTTP uçlarını çağırmak için ince yardımcı. */
public final class ApiIstemcisi {

    private final MockMvcTester mvc;
    private final String yol;

    public ApiIstemcisi(MockMvcTester mvc, String yol) {
        this.mvc = mvc;
        this.yol = yol;
    }

    public MvcTestResult post(IstekGovdesi govde) {
        return mvc.post().uri(yol).contentType(MediaType.APPLICATION_JSON).content(govde.json()).exchange();
    }

    public MvcTestResult put(long id, IstekGovdesi govde) {
        return mvc.put().uri(yol + "/" + id).contentType(MediaType.APPLICATION_JSON).content(govde.json()).exchange();
    }

    public MvcTestResult get(long id) {
        return mvc.get().uri(yol + "/" + id).exchange();
    }

    public MvcTestResult delete(long id) {
        return mvc.delete().uri(yol + "/" + id).exchange();
    }

    /** POST eder, 201 bekler ve yeni kaydın ID'sini döndürür. */
    public long olustur(IstekGovdesi govde) {
        MvcTestResult sonuc = post(govde);
        assertThat(sonuc).as("POST %s: %s", yol, govde.json()).hasStatus(201);
        return ((Number) JsonPath.read(govde(sonuc), "$.id")).longValue();
    }

    /** Son sayfalardaki (ID'ye göre azalan) aktif kayıtların ID'leri. */
    public List<Integer> listedekiIdler() {
        MvcTestResult sonuc = mvc.get().uri(yol + "?size=100&sort=id,desc").exchange();
        assertThat(sonuc).hasStatusOk();
        return JsonPath.read(govde(sonuc), "$.icerik[*].id");
    }

    public static String govde(MvcTestResult sonuc) {
        try {
            return sonuc.getResponse().getContentAsString();
        } catch (UnsupportedEncodingException hata) {
            throw new IllegalStateException(hata);
        }
    }
}
