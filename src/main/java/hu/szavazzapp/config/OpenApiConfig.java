package hu.szavazzapp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI szavazzAppOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SzavazzApp API")
                        .version("0.3.0")
                        .description("SzavazzApp automatikusan generált REST API dokumentáció."));
    }
}