package io.e2d.projectcollab.task.service;

import io.e2d.projectcollab.common.exception.ApiException;
import io.e2d.projectcollab.common.exception.ErrorCode;
import io.e2d.projectcollab.project.domain.Project;
import io.e2d.projectcollab.project.domain.ProjectMember;
import io.e2d.projectcollab.project.service.ProjectAuthorizationService;
import io.e2d.projectcollab.project.service.ProjectService;
import io.e2d.projectcollab.task.domain.Task;
import io.e2d.projectcollab.task.domain.TaskStatus;
import io.e2d.projectcollab.task.dto.TaskDtos.CreateTaskRequest;
import io.e2d.projectcollab.task.dto.TaskDtos.TaskPageResponse;
import io.e2d.projectcollab.task.dto.TaskDtos.TaskResponse;
import io.e2d.projectcollab.task.dto.TaskDtos.UpdateTaskRequest;
import io.e2d.projectcollab.task.repository.TaskRepository;
import io.e2d.projectcollab.user.domain.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectService projectService;
    private final ProjectAuthorizationService authorizationService;

    public TaskService(
            TaskRepository taskRepository,
            ProjectService projectService,
            ProjectAuthorizationService authorizationService
    ) {
        this.taskRepository = taskRepository;
        this.projectService = projectService;
        this.authorizationService = authorizationService;
    }

    @Transactional
    public TaskResponse create(
            Long requesterId,
            Long projectId,
            CreateTaskRequest request
    ) {
        Project project = projectService.getEntity(projectId);
        authorizationService.requireMember(projectId, requesterId);
        User assignee = resolveAssignee(projectId, request.assigneeId());
        TaskStatus status = request.status() == null ? TaskStatus.TODO : request.status();

        Task task = taskRepository.saveAndFlush(Task.create(
                project,
                request.title().trim(),
                normalizeDescription(request.description()),
                status,
                assignee
        ));
        return TaskResponse.from(task);
    }

    public TaskPageResponse getAll(
            Long requesterId,
            Long projectId,
            String keyword,
            TaskStatus status,
            int page,
            int size
    ) {
        projectService.getEntity(projectId);
        authorizationService.requireMember(projectId, requesterId);
        validatePagination(page, size);

        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
                        .and(Sort.by(Sort.Direction.DESC, "id"))
        );
        return TaskPageResponse.from(taskRepository.search(
                projectId,
                normalizeKeyword(keyword),
                status,
                pageRequest
        ));
    }

    public TaskResponse get(Long requesterId, Long projectId, Long taskId) {
        projectService.getEntity(projectId);
        authorizationService.requireMember(projectId, requesterId);
        return TaskResponse.from(getEntity(projectId, taskId));
    }

    @Transactional
    public TaskResponse update(
            Long requesterId,
            Long projectId,
            Long taskId,
            UpdateTaskRequest request
    ) {
        projectService.getEntity(projectId);
        ProjectMember requester = authorizationService.requireMember(projectId, requesterId);
        Task task = getEntity(projectId, taskId);
        validateCanModify(requester, task);
        validateVersion(task, request.version());
        task.update(
                request.title().trim(),
                normalizeDescription(request.description()),
                request.status(),
                resolveAssignee(projectId, request.assigneeId())
        );
        taskRepository.flush();
        return TaskResponse.from(task);
    }

    @Transactional
    public void delete(Long requesterId, Long projectId, Long taskId) {
        projectService.getEntity(projectId);
        ProjectMember requester = authorizationService.requireMember(projectId, requesterId);
        Task task = getEntity(projectId, taskId);
        validateCanModify(requester, task);
        taskRepository.delete(task);
    }

    private Task getEntity(Long projectId, Long taskId) {
        return taskRepository.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new ApiException(ErrorCode.TASK_NOT_FOUND));
    }

    private User resolveAssignee(Long projectId, Long assigneeId) {
        return assigneeId == null
                ? null
                : authorizationService.requireAssignableMember(projectId, assigneeId).getUser();
    }

    private void validateCanModify(ProjectMember requester, Task task) {
        boolean isAssignee = task.getAssignee() != null
                && task.getAssignee().getId().equals(requester.getUser().getId());
        if (!authorizationService.isManager(requester) && !isAssignee) {
            throw new ApiException(ErrorCode.TASK_PERMISSION_DENIED);
        }
    }

    private void validateVersion(Task task, Long requestedVersion) {
        if (!task.getVersion().equals(requestedVersion)) {
            throw new ApiException(ErrorCode.TASK_VERSION_CONFLICT);
        }
    }

    private String normalizeDescription(String description) {
        return description == null ? null : description.trim();
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null || keyword.isBlank() ? null : keyword.trim();
    }

    private void validatePagination(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new ApiException(ErrorCode.INVALID_PAGINATION);
        }
    }
}
