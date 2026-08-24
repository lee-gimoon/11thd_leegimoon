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

@Service
@Transactional(readOnly = true)
public class ProjectAuthorizationService {

    private static final Set<ProjectRole> PROJECT_MANAGERS =
            EnumSet.of(ProjectRole.OWNER, ProjectRole.ADMIN);

    private final ProjectMemberRepository projectMemberRepository;
    private final UserService userService;

    public ProjectAuthorizationService(
            ProjectMemberRepository projectMemberRepository,
            UserService userService
    ) {
        this.projectMemberRepository = projectMemberRepository;
        this.userService = userService;
    }

    public ProjectMember requireMember(Long projectId, Long requesterId) {
        userService.getEntity(requesterId);
        return projectMemberRepository.findByProjectIdAndUserId(projectId, requesterId)
                .orElseThrow(() -> new ApiException(ErrorCode.PROJECT_ACCESS_DENIED));
    }

    public ProjectMember requireManager(Long projectId, Long requesterId) {
        ProjectMember member = requireMember(projectId, requesterId);
        if (!PROJECT_MANAGERS.contains(member.getRole())) {
            throw new ApiException(ErrorCode.PROJECT_PERMISSION_DENIED);
        }
        return member;
    }

    public ProjectMember requireOwner(Long projectId, Long requesterId) {
        ProjectMember member = requireMember(projectId, requesterId);
        if (member.getRole() != ProjectRole.OWNER) {
            throw new ApiException(ErrorCode.PROJECT_PERMISSION_DENIED);
        }
        return member;
    }

    public ProjectMember requireAssignableMember(Long projectId, Long assigneeId) {
        userService.getEntity(assigneeId);
        return projectMemberRepository.findByProjectIdAndUserId(projectId, assigneeId)
                .orElseThrow(() -> new ApiException(ErrorCode.ASSIGNEE_NOT_PROJECT_MEMBER));
    }

    public boolean isManager(ProjectMember member) {
        return PROJECT_MANAGERS.contains(member.getRole());
    }
}
