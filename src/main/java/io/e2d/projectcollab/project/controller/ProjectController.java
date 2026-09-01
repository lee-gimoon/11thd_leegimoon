// 목적: 프로젝트 생성·조회·수정·삭제 HTTP 요청을 처리하기 위해 만들어진 파일입니다.
// 역할: 프로젝트 API 요청을 서비스 계층에 전달하고 결과를 반환합니다.
package io.e2d.projectcollab.project.controller;

import io.e2d.projectcollab.project.dto.ProjectDtos.CreateProjectRequest;
import io.e2d.projectcollab.project.dto.ProjectDtos.ProjectResponse;
import io.e2d.projectcollab.project.dto.ProjectDtos.UpdateProjectRequest;
import io.e2d.projectcollab.project.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static io.e2d.projectcollab.common.web.RequesterHeaders.REQUESTER_ID;

// 역할: 프로젝트 기본 관리 REST API를 제공합니다.
@Tag(name = "Projects", description = "프로젝트 기본 관리")
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    // 역할: 프로젝트 서비스 의존성을 주입받아 컨트롤러를 생성합니다.
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    // 역할: 요청자를 소유자로 지정해 새 프로젝트를 생성합니다.
    @Operation(summary = "프로젝트 생성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(
            @RequestHeader(REQUESTER_ID) Long requesterId,
            @Valid @RequestBody CreateProjectRequest request
    ) {
        return projectService.create(requesterId, request);
    }

    // 역할: 요청자가 참여 중인 프로젝트 목록을 조회합니다.
    @Operation(summary = "내 프로젝트 목록 조회")
    @GetMapping
    public List<ProjectResponse> getMyProjects(
            @RequestHeader(REQUESTER_ID) Long requesterId
    ) {
        return projectService.getMyProjects(requesterId);
    }

    // 역할: 접근 권한을 확인한 뒤 지정한 프로젝트를 조회합니다.
    @Operation(summary = "프로젝트 조회")
    @GetMapping("/{projectId}")
    public ProjectResponse get(
            @RequestHeader(REQUESTER_ID) Long requesterId,
            @PathVariable Long projectId
    ) {
        return projectService.get(requesterId, projectId);
    }

    // 역할: 관리 권한을 확인한 뒤 프로젝트 정보를 수정합니다.
    @Operation(summary = "프로젝트 수정")
    @PutMapping("/{projectId}")
    public ProjectResponse update(
            @RequestHeader(REQUESTER_ID) Long requesterId,
            @PathVariable Long projectId,
            @Valid @RequestBody UpdateProjectRequest request
    ) {
        return projectService.update(requesterId, projectId, request);
    }

    // 역할: 소유자 권한을 확인한 뒤 프로젝트를 삭제합니다.
    @Operation(summary = "프로젝트 삭제")
    @DeleteMapping("/{projectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestHeader(REQUESTER_ID) Long requesterId,
            @PathVariable Long projectId
    ) {
        projectService.delete(requesterId, projectId);
    }
}
