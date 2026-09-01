// 목적: 프로젝트 멤버와 역할 관리 HTTP 요청을 처리하기 위해 만들어진 파일입니다.
// 역할: 멤버 조회·추가·역할 변경·제거 요청을 서비스 계층에 전달합니다.
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

// 역할: 프로젝트 멤버 및 역할 관리 REST API를 제공합니다.
@Tag(name = "Project Members", description = "프로젝트 멤버와 역할 관리")
@RestController
@RequestMapping("/api/projects/{projectId}/members")
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    // 역할: 프로젝트 멤버 서비스 의존성을 주입받아 컨트롤러를 생성합니다.
    public ProjectMemberController(ProjectMemberService projectMemberService) {
        this.projectMemberService = projectMemberService;
    }

    // 역할: 프로젝트에 속한 모든 멤버를 조회합니다.
    @Operation(summary = "프로젝트 멤버 목록 조회")
    @GetMapping
    public List<ProjectMemberResponse> getAll(
            @RequestHeader(REQUESTER_ID) Long requesterId,
            @PathVariable Long projectId
    ) {
        return projectMemberService.getAll(requesterId, projectId);
    }

    // 역할: 관리 권한을 확인한 뒤 프로젝트에 멤버를 추가합니다.
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

    // 역할: 관리 권한을 확인한 뒤 프로젝트 멤버의 역할을 변경합니다.
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

    // 역할: 관리 권한을 확인한 뒤 프로젝트에서 멤버를 제거합니다.
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
