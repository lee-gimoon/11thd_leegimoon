package io.e2d.projectcollab.project.controller;

import io.e2d.projectcollab.project.dto.ProjectDtos.AddProjectMemberRequest;
import io.e2d.projectcollab.project.dto.ProjectDtos.ChangeMemberRoleRequest;
import io.e2d.projectcollab.project.dto.ProjectDtos.ProjectMemberResponse;
import io.e2d.projectcollab.project.service.ProjectMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static io.e2d.projectcollab.common.web.RequesterHeaders.REQUESTER_ID;

@Tag(name = "Project Members", description = "프로젝트 멤버와 역할 관리")
@RestController
@RequestMapping("/api/projects/{projectId}/members")
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    public ProjectMemberController(ProjectMemberService projectMemberService) {
        this.projectMemberService = projectMemberService;
    }

    @Operation(summary = "프로젝트 멤버 목록 조회")
    @GetMapping
    public List<ProjectMemberResponse> getAll(
            @RequestHeader(REQUESTER_ID) Long requesterId,
            @PathVariable Long projectId
    ) {
        return projectMemberService.getAll(requesterId, projectId);
    }

    @Operation(summary = "프로젝트 멤버 추가")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectMemberResponse add(
            @RequestHeader(REQUESTER_ID) Long requesterId,
            @PathVariable Long projectId,
            @Valid @RequestBody AddProjectMemberRequest request
    ) {
        return projectMemberService.add(requesterId, projectId, request);
    }

    @Operation(summary = "프로젝트 멤버 역할 변경")
    @PatchMapping("/{userId}")
    public ProjectMemberResponse changeRole(
            @RequestHeader(REQUESTER_ID) Long requesterId,
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @Valid @RequestBody ChangeMemberRoleRequest request
    ) {
        return projectMemberService.changeRole(requesterId, projectId, userId, request);
    }

    @Operation(summary = "프로젝트 멤버 제거")
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(
            @RequestHeader(REQUESTER_ID) Long requesterId,
            @PathVariable Long projectId,
            @PathVariable Long userId
    ) {
        projectMemberService.remove(requesterId, projectId, userId);
    }
}
