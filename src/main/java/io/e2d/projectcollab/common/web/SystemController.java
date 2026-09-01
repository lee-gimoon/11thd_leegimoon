// 목적: 애플리케이션의 실행 상태를 외부에서 확인할 수 있도록 만들어진 파일입니다.
// 역할: 시스템 상태 확인 API와 응답 형식을 제공합니다.
package io.e2d.projectcollab.common.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 역할: 애플리케이션 상태 확인 요청을 처리합니다.
@Tag(name = "System", description = "애플리케이션 상태 확인")
@RestController
@RequestMapping("/api/health")
public class SystemController {

    // 역할: 현재 애플리케이션의 정상 실행 상태를 반환합니다.
    @Operation(summary = "애플리케이션 상태 조회")
    @GetMapping
    public HealthResponse health() {
        return new HealthResponse("UP", "project-collab");
    }

    // 역할: 상태 코드와 애플리케이션 이름을 담은 응답 데이터를 표현합니다.
    public record HealthResponse(String status, String application) {
    }
}
