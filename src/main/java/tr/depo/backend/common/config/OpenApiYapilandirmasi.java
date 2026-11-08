package tr.depo.backend.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** OpenAPI (Swagger UI: /swagger-ui.html, şema: /v3/api-docs). */
@Configuration
public class OpenApiYapilandirmasi {

    private static final String BASLIK = "Kaju Backend API";
    private static final String ACIKLAMA = "Depo / envanter uygulamasının CRUD API'si. Silme işlemleri soft delete'tir; "
        + "hata cevapları RFC 9457 ProblemDetail biçimindedir.";
    private static final String API_SURUMU = "v1";

    @Bean
    public OpenAPI kajuOpenApi() {
        return new OpenAPI().info(new Info().title(BASLIK).description(ACIKLAMA).version(API_SURUMU));
    }
}
