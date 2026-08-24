package io.e2d.projectcollab.project.service;

import io.e2d.projectcollab.common.exception.ApiException;
import io.e2d.projectcollab.common.exception.ErrorCode;
import io.e2d.projectcollab.project.domain.Project;
import io.e2d.projectcollab.project.domain.ProjectMember;
import io.e2d.projectcollab.project.dto.ProjectDtos.AddProjectMemberRequest;
import io.e2d.projectcollab.project.dto.ProjectDtos.ChangeMemberRoleRequest;
import io.e2d.projectcollab.project.dto.ProjectDtos.ProjectMemberResponse;
import io.e2d.projectcollab.project.repository.ProjectMemberRepository;
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

    public ProjectMemberService(
            ProjectMemberRepository projectMemberRepository,
            ProjectService projectService,
            UserService userService
    ) {
        this.projectMemberRepository = projectMemberRepository;
        this.projectService = projectService;
        this.userService = userService;
    }

    public List<ProjectMemberResponse> getAll(Long requesterId, Long projectId) {
        userService.getEntity(requesterId);
        projectService.getEntity(projectId);
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
        userService.getEntity(requesterId);
        Project project = projectService.getEntity(projectId);
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
        userService.getEntity(requesterId);
        projectService.getEntity(projectId);
        ProjectMember member = getEntity(projectId, userId);
        member.changeRole(request.role());
        return ProjectMemberResponse.from(member);
    }

    @Transactional
    public void remove(Long requesterId, Long projectId, Long userId) {
        userService.getEntity(requesterId);
        projectService.getEntity(projectId);
        projectMemberRepository.delete(getEntity(projectId, userId));
    }

    private ProjectMember getEntity(Long projectId, Long userId) {
        return projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
