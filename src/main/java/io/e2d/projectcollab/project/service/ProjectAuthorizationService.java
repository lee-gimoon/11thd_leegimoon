// 목적: 프로젝트 접근과 관리 권한 규칙을 한곳에서 검사하기 위해 만들어진 파일입니다.
// 역할: 요청자의 멤버십과 역할을 확인하고 권한 부족 예외를 발생시킵니다.
package io.e2d.projectcollab.project.service;

import io.e2d.projectcollab.common.exception.ApiException;
import io.e2d.projectcollab.common.exception.ErrorCode;
import io.e2d.projectcollab.project.domain.ProjectMember;
import io.e2d.projectcollab.project.domain.ProjectRole;
import io.e2d.projectcollab.project.repository.ProjectMemberRepository;
import io.e2d.projectcollab.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Set;

// 역할: 프로젝트 멤버·관리자·소유자 및 작업 담당자 자격을 검증합니다.
@Service
@Transactional(readOnly = true)
public class ProjectAuthorizationService {

    private static final Set<ProjectRole> PROJECT_MANAGERS =
            EnumSet.of(ProjectRole.OWNER, ProjectRole.ADMIN);

    private final ProjectMemberRepository projectMemberRepository;
    private final UserService userService;

    // 역할: 멤버 저장소와 사용자 서비스 의존성을 주입받아 권한 서비스를 생성합니다.
    public ProjectAuthorizationService(
            ProjectMemberRepository projectMemberRepository,
            UserService userService
    ) {
        this.projectMemberRepository = projectMemberRepository;
        this.userService = userService;
    }

    // 역할: 요청자가 프로젝트 멤버인지 확인하고 해당 멤버십을 반환합니다.
    public ProjectMember requireMember(Long projectId, Long requesterId) {
        userService.getEntity(requesterId);
        return projectMemberRepository.findByProjectIdAndUserId(projectId, requesterId)
                .orElseThrow(() -> new ApiException(ErrorCode.PROJECT_ACCESS_DENIED));
    }

    // 역할: 요청자가 프로젝트 소유자 또는 관리자인지 확인합니다.
    public ProjectMember requireManager(Long projectId, Long requesterId) {
        ProjectMember member = requireMember(projectId, requesterId);
        if (!PROJECT_MANAGERS.contains(member.getRole())) {
            throw new ApiException(ErrorCode.PROJECT_PERMISSION_DENIED);
        }
        return member;
    }

    // 역할: 요청자가 프로젝트 소유자인지 확인합니다.
    public ProjectMember requireOwner(Long projectId, Long requesterId) {
        ProjectMember member = requireMember(projectId, requesterId);
        if (member.getRole() != ProjectRole.OWNER) {
            throw new ApiException(ErrorCode.PROJECT_PERMISSION_DENIED);
        }
        return member;
    }

    // 역할: 지정한 사용자가 작업 담당자로 배정 가능한 프로젝트 멤버인지 확인합니다.
    public ProjectMember requireAssignableMember(Long projectId, Long assigneeId) {
        userService.getEntity(assigneeId);
        return projectMemberRepository.findByProjectIdAndUserId(projectId, assigneeId)
                .orElseThrow(() -> new ApiException(ErrorCode.ASSIGNEE_NOT_PROJECT_MEMBER));
    }

    // 역할: 주어진 프로젝트 멤버가 소유자 또는 관리자인지 반환합니다.
    public boolean isManager(ProjectMember member) {
        return PROJECT_MANAGERS.contains(member.getRole());
    }
}
