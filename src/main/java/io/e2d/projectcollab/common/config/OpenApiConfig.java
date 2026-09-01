// 목적: 프로젝트 협업 API의 문서화 설정을 구성하기 위해 만들어진 파일입니다.
// 역할: OpenAPI 기본 정보와 태그 표시 순서를 등록합니다.
package io.e2d.projectcollab.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;
import java.util.List;

// 역할: Swagger 문서에 사용할 OpenAPI 빈과 사용자 지정 정렬 규칙을 구성합니다.
@Configuration
public class OpenApiConfig {

    private static final List<String> TAG_ORDER = List.of(
            "Users",
            "Projects",
            "Project Members",
            "Tasks",
            "System"
    );

    // 역할: 프로젝트 협업 API의 제목, 설명, 버전, 태그 정보를 생성합니다.
    @Bean
    public OpenAPI projectCollabOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Project Collab API")
                        .description("프로젝트와 작업을 함께 관리하는 협업 서비스 API")
                        .version("v1"))
                .tags(List.of(
                        new Tag().name("Users").description("사용자 등록 및 조회"),
                        new Tag().name("Projects").description("프로젝트 기본 관리"),
                        new Tag().name("Project Members").description("프로젝트 멤버와 역할 관리"),
                        new Tag().name("Tasks").description("프로젝트 작업 기본 관리"),
                        new Tag().name("System").description("애플리케이션 상태 확인")
                ));
    }

    // 역할: 문서에 표시되는 API 태그를 정의된 업무 순서대로 정렬합니다.
    @Bean
    public OpenApiCustomizer openApiTagOrderCustomizer() {
        return openApi -> {
            if (openApi.getTags() == null) {
                return;
            }

            openApi.getTags().sort(Comparator.comparingInt(tag -> {
                int order = TAG_ORDER.indexOf(tag.getName());
                return order >= 0 ? order : Integer.MAX_VALUE;
            }));
        };
    }
}
