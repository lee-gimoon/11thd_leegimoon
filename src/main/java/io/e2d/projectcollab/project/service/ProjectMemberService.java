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

@Service
@Transactional(readOnly = true)
public class ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectService projectService;
    private final UserService userService;
    private final ProjectAuthorizationService authorizationService;
    private final TaskRepository taskRepository;

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

    public List<ProjectMemberResponse> getAll(Long requesterId, Long projectId) {
        projectService.getEntity(projectId);
        authorizationService.requireMember(projectId, requesterId);
        return projectMemberRepository.findAllWithUserByProjectId(projectId).stream()
                .map(ProjectMemberResponse::from)
                .toList();
    }

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

    @Transactional
    public void remove(Long requesterId, Long projectId, Long userId) {
        projectService.getEntity(projectId);
        authorizationService.requireManager(projectId, requesterId);
        ProjectMember member = getEntity(projectId, userId);
        validateOwnerRemains(projectId, member, null);
        taskRepository.clearAssigneeByProjectIdAndUserId(projectId, userId);
        projectMemberRepository.delete(member);
    }

    private ProjectMember getEntity(Long projectId, Long userId) {
        return projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));
    }

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
