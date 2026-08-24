package io.e2d.projectcollab.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI projectCollabOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Project Collab API")
                        .description("프로젝트와 작업을 함께 관리하는 협업 서비스 API")
                        .version("v1"));
    }
}
