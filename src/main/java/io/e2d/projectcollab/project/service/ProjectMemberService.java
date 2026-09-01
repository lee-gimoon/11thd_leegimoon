// 목적: 프로젝트 멤버와 역할을 관리하는 업무 규칙을 처리하기 위해 만들어진 파일입니다.
// 역할: 멤버 조회·추가·역할 변경·제거와 마지막 소유자 보호를 수행합니다.
package io.e2d.projectcollab.project.service;

import io.e2d.projectcollab.common.exception.ApiException;
import io.e2d.projectcollab.common.exception.ErrorCode;
import io.e2d.projectcollab.project.domain.Project;
import io.e2d.projectcollab.project.domain.ProjectMember;
import io.e2d.projectcollab.project.domain.ProjectRole;
import io.e2d.projectcollab.project.dto.ProjectDtos.AddProjectMemberRequest;
import io.e2d.projectcollab.project.dto.ProjectDtos.ChangeMemberRoleRequest;
import io.e2d.projectcollab.project.dto.ProjectDtos.ProjectMemberResponse;
import io.e2d.projectcollab.project.repository.ProjectMemberRepository;
import io.e2d.projectcollab.task.repository.TaskRepository;
import io.e2d.projectcollab.user.domain.User;
import io.e2d.projectcollab.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 역할: 프로젝트 멤버십의 전체 생명주기와 역할 변경 규칙을 담당합니다.
@Service
@Transactional(readOnly = true)
public class ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectService projectService;
    private final UserService userService;
    private final ProjectAuthorizationService authorizationService;
    private final TaskRepository taskRepository;

    // 역할: 멤버 관리에 필요한 저장소와 서비스 의존성을 주입받습니다.
    public ProjectMemberService(
            ProjectMemberRepository projectMemberRepository,
            ProjectService projectService,
            UserService userService,
            ProjectAuthorizationService authorizationService,
            TaskRepository taskRepository
    ) {
        this.projectMemberRepository = projectMemberRepository;
        this.projectService = projectService;
        this.userService = userService;
        this.authorizationService = authorizationService;
        this.taskRepository = taskRepository;
    }

    // 역할: 접근 권한을 확인하고 프로젝트의 모든 멤버를 조회합니다.
    public List<ProjectMemberResponse> getAll(Long requesterId, Long projectId) {
        projectService.getEntity(projectId);
        authorizationService.requireMember(projectId, requesterId);
        return projectMemberRepository.findAllWithUserByProjectId(projectId).stream()
                .map(ProjectMemberResponse::from)
                .toList();
    }

    // 역할: 관리 권한과 중복 여부를 확인한 뒤 프로젝트에 멤버를 추가합니다.
    @Transactional
    public ProjectMemberResponse add(
            Long requesterId,
            Long projectId,
            AddProjectMemberRequest request
    ) {
        Project project = projectService.getEntity(projectId);
        authorizationService.requireManager(projectId, requesterId);
        User user = userService.getEntity(request.userId());

        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, user.getId())) {
            throw new ApiException(ErrorCode.MEMBER_ALREADY_EXISTS);
        }

        ProjectMember member = projectMemberRepository.save(
                ProjectMember.create(project, user, request.role())
        );
        return ProjectMemberResponse.from(member);
    }

    // 역할: 관리 권한과 소유자 유지 규칙을 확인한 뒤 멤버 역할을 변경합니다.
    @Transactional
    public ProjectMemberResponse changeRole(
            Long requesterId,
            Long projectId,
            Long userId,
            ChangeMemberRoleRequest request
    ) {
        projectService.getEntity(projectId);
        authorizationService.requireManager(projectId, requesterId);
        ProjectMember member = getEntity(projectId, userId);
        validateOwnerRemains(projectId, member, request.role());
        member.changeRole(request.role());
        return ProjectMemberResponse.from(member);
    }

    // 역할: 관리 권한과 소유자 유지 규칙을 확인한 뒤 담당 작업을 해제하고 멤버를 제거합니다.
    @Transactional
    public void remove(Long requesterId, Long projectId, Long userId) {
        projectService.getEntity(projectId);
        authorizationService.requireManager(projectId, requesterId);
        ProjectMember member = getEntity(projectId, userId);
        validateOwnerRemains(projectId, member, null);
        taskRepository.clearAssigneeByProjectIdAndUserId(projectId, userId);
        projectMemberRepository.delete(member);
    }

    // 역할: 프로젝트와 사용자로 멤버십을 조회하고 없으면 멤버 없음 예외를 발생시킵니다.
    private ProjectMember getEntity(Long projectId, Long userId) {
        return projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));
    }

    // 역할: 역할 변경이나 제거 후에도 프로젝트에 최소 한 명의 소유자가 남는지 검증합니다.
    private void validateOwnerRemains(
            Long projectId,
            ProjectMember member,
            ProjectRole nextRole
    ) {
        boolean removesOwnerRole = member.getRole() == ProjectRole.OWNER
                && nextRole != ProjectRole.OWNER;
        if (removesOwnerRole
                && projectMemberRepository.countByProjectIdAndRole(projectId, ProjectRole.OWNER) <= 1) {
            throw new ApiException(ErrorCode.LAST_OWNER_REQUIRED);
        }
    }
}
