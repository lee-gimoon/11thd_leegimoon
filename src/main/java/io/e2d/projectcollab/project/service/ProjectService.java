package io.e2d.projectcollab.project.service;

import io.e2d.projectcollab.common.exception.ApiException;
import io.e2d.projectcollab.common.exception.ErrorCode;
import io.e2d.projectcollab.project.domain.Project;
import io.e2d.projectcollab.project.domain.ProjectMember;
import io.e2d.projectcollab.project.domain.ProjectRole;
import io.e2d.projectcollab.project.dto.ProjectDtos.CreateProjectRequest;
import io.e2d.projectcollab.project.dto.ProjectDtos.ProjectResponse;
import io.e2d.projectcollab.project.dto.ProjectDtos.UpdateProjectRequest;
import io.e2d.projectcollab.project.repository.ProjectMemberRepository;
import io.e2d.projectcollab.project.repository.ProjectRepository;
import io.e2d.projectcollab.task.repository.TaskRepository;
import io.e2d.projectcollab.user.domain.User;
import io.e2d.projectcollab.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskRepository taskRepository;
    private final UserService userService;

    public ProjectService(
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            TaskRepository taskRepository,
            UserService userService
    ) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.taskRepository = taskRepository;
        this.userService = userService;
    }

    @Transactional
    public ProjectResponse create(Long requesterId, CreateProjectRequest request) {
        User creator = userService.getEntity(requesterId);
        Project project = projectRepository.save(Project.create(
                request.name().trim(),
                normalizeDescription(request.description())
        ));
        projectMemberRepository.save(ProjectMember.create(project, creator, ProjectRole.OWNER));
        return ProjectResponse.from(project);
    }

    public List<ProjectResponse> getMyProjects(Long requesterId) {
        userService.getEntity(requesterId);
        return projectMemberRepository.findAllWithProjectByUserId(requesterId).stream()
                .map(ProjectMember::getProject)
                .map(ProjectResponse::from)
                .toList();
    }

    public ProjectResponse get(Long requesterId, Long projectId) {
        userService.getEntity(requesterId);
        return ProjectResponse.from(getEntity(projectId));
    }

    @Transactional
    public ProjectResponse update(Long requesterId, Long projectId, UpdateProjectRequest request) {
        userService.getEntity(requesterId);
        Project project = getEntity(projectId);
        project.update(request.name().trim(), normalizeDescription(request.description()));
        return ProjectResponse.from(project);
    }

    @Transactional
    public void delete(Long requesterId, Long projectId) {
        userService.getEntity(requesterId);
        Project project = getEntity(projectId);
        taskRepository.deleteAllByProjectId(projectId);
        projectMemberRepository.deleteAllByProjectId(projectId);
        projectRepository.delete(project);
    }

    public Project getEntity(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException(ErrorCode.PROJECT_NOT_FOUND));
    }

    private String normalizeDescription(String description) {
        return description == null ? null : description.trim();
    }
}
