package io.e2d.projectcollab.task.service;

import io.e2d.projectcollab.common.exception.ApiException;
import io.e2d.projectcollab.common.exception.ErrorCode;
import io.e2d.projectcollab.project.domain.Project;
import io.e2d.projectcollab.project.service.ProjectService;
import io.e2d.projectcollab.task.domain.Task;
import io.e2d.projectcollab.task.domain.TaskStatus;
import io.e2d.projectcollab.task.dto.TaskDtos.CreateTaskRequest;
import io.e2d.projectcollab.task.dto.TaskDtos.TaskResponse;
import io.e2d.projectcollab.task.dto.TaskDtos.UpdateTaskRequest;
import io.e2d.projectcollab.task.repository.TaskRepository;
import io.e2d.projectcollab.user.domain.User;
import io.e2d.projectcollab.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectService projectService;
    private final UserService userService;

    public TaskService(
            TaskRepository taskRepository,
            ProjectService projectService,
            UserService userService
    ) {
        this.taskRepository = taskRepository;
        this.projectService = projectService;
        this.userService = userService;
    }

    @Transactional
    public TaskResponse create(
            Long requesterId,
            Long projectId,
            CreateTaskRequest request
    ) {
        userService.getEntity(requesterId);
        Project project = projectService.getEntity(projectId);
        User assignee = resolveAssignee(request.assigneeId());
        TaskStatus status = request.status() == null ? TaskStatus.TODO : request.status();

        Task task = taskRepository.save(Task.create(
                project,
                request.title().trim(),
                normalizeDescription(request.description()),
                status,
                assignee
        ));
        return TaskResponse.from(task);
    }

    public List<TaskResponse> getAll(Long requesterId, Long projectId) {
        userService.getEntity(requesterId);
        projectService.getEntity(projectId);
        return taskRepository.findAllByProjectIdOrderByCreatedAtDesc(projectId).stream()
                .map(TaskResponse::from)
                .toList();
    }

    public TaskResponse get(Long requesterId, Long projectId, Long taskId) {
        userService.getEntity(requesterId);
        projectService.getEntity(projectId);
        return TaskResponse.from(getEntity(projectId, taskId));
    }

    @Transactional
    public TaskResponse update(
            Long requesterId,
            Long projectId,
            Long taskId,
            UpdateTaskRequest request
    ) {
        userService.getEntity(requesterId);
        projectService.getEntity(projectId);
        Task task = getEntity(projectId, taskId);
        task.update(
                request.title().trim(),
                normalizeDescription(request.description()),
                request.status(),
                resolveAssignee(request.assigneeId())
        );
        return TaskResponse.from(task);
    }

    @Transactional
    public void delete(Long requesterId, Long projectId, Long taskId) {
        userService.getEntity(requesterId);
        projectService.getEntity(projectId);
        taskRepository.delete(getEntity(projectId, taskId));
    }

    private Task getEntity(Long projectId, Long taskId) {
        return taskRepository.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new ApiException(ErrorCode.TASK_NOT_FOUND));
    }

    private User resolveAssignee(Long assigneeId) {
        return assigneeId == null ? null : userService.getEntity(assigneeId);
    }

    private String normalizeDescription(String description) {
        return description == null ? null : description.trim();
    }
}
