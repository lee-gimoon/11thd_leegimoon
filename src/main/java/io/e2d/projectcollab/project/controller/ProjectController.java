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

@Tag(name = "Projects", description = "프로젝트 기본 관리")
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Operation(summary = "프로젝트 생성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(
            @RequestHeader(REQUESTER_ID) Long requesterId,
            @Valid @RequestBody CreateProjectRequest request
    ) {
        return projectService.create(requesterId, request);
    }

    @Operation(summary = "내 프로젝트 목록 조회")
    @GetMapping
    public List<ProjectResponse> getMyProjects(
            @RequestHeader(REQUESTER_ID) Long requesterId
    ) {
        return projectService.getMyProjects(requesterId);
    }

    @Operation(summary = "프로젝트 조회")
    @GetMapping("/{projectId}")
    public ProjectResponse get(
            @RequestHeader(REQUESTER_ID) Long requesterId,
            @PathVariable Long projectId
    ) {
        return projectService.get(requesterId, projectId);
    }

    @Operation(summary = "프로젝트 수정")
    @PutMapping("/{projectId}")
    public ProjectResponse update(
            @RequestHeader(REQUESTER_ID) Long requesterId,
            @PathVariable Long projectId,
            @Valid @RequestBody UpdateProjectRequest request
    ) {
        return projectService.update(requesterId, projectId, request);
    }

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
