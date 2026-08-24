package io.e2d.projectcollab.common.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "System", description = "애플리케이션 상태 확인")
@RestController
@RequestMapping("/api/health")
public class SystemController {

    @Operation(summary = "애플리케이션 상태 조회")
    @GetMapping
    public HealthResponse health() {
        return new HealthResponse("UP", "project-collab");
    }

    public record HealthResponse(String status, String application) {
    }
}
