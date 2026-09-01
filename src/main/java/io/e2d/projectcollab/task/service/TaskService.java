// 목적: 프로젝트 작업의 생성·검색·조회·수정·삭제 업무 규칙을 처리하기 위해 만들어진 파일입니다.
// 역할: 작업 권한, 담당자 자격, 버전 충돌과 페이지 조건을 검증합니다.
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

// 역할: 프로젝트 작업의 전체 생명주기와 검색 및 동시성 제어를 담당합니다.
@Service
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectService projectService;
    private final ProjectAuthorizationService authorizationService;

    // 역할: 작업 관리에 필요한 저장소와 프로젝트 서비스 의존성을 주입받습니다.
    public TaskService(
            TaskRepository taskRepository,
            ProjectService projectService,
            ProjectAuthorizationService authorizationService
    ) {
        this.taskRepository = taskRepository;
        this.projectService = projectService;
        this.authorizationService = authorizationService;
    }

    // 역할: 프로젝트 접근 권한과 담당자 자격을 확인한 뒤 새 작업을 생성합니다.
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

    // 역할: 접근 권한과 페이지 조건을 확인한 뒤 작업을 검색해 페이지 응답으로 반환합니다.
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

    // 역할: 프로젝트 접근 권한을 확인하고 지정한 작업을 반환합니다.
    public TaskResponse get(Long requesterId, Long projectId, Long taskId) {
        projectService.getEntity(projectId);
        authorizationService.requireMember(projectId, requesterId);
        return TaskResponse.from(getEntity(projectId, taskId));
    }

    // 역할: 수정 권한과 요청 버전을 확인한 뒤 작업 정보를 갱신합니다.
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

    // 역할: 수정 권한을 확인한 뒤 지정한 작업을 삭제합니다.
    @Transactional
    public void delete(Long requesterId, Long projectId, Long taskId) {
        projectService.getEntity(projectId);
        ProjectMember requester = authorizationService.requireMember(projectId, requesterId);
        Task task = getEntity(projectId, taskId);
        validateCanModify(requester, task);
        taskRepository.delete(task);
    }

    // 역할: 프로젝트와 작업 ID로 엔티티를 조회하고 없으면 작업 없음 예외를 발생시킵니다.
    private Task getEntity(Long projectId, Long taskId) {
        return taskRepository.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new ApiException(ErrorCode.TASK_NOT_FOUND));
    }

    // 역할: 담당자 ID가 있으면 프로젝트 멤버 자격을 확인해 사용자 엔티티로 반환합니다.
    private User resolveAssignee(Long projectId, Long assigneeId) {
        return assigneeId == null
                ? null
                : authorizationService.requireAssignableMember(projectId, assigneeId).getUser();
    }

    // 역할: 요청자가 프로젝트 관리자이거나 해당 작업 담당자인지 검증합니다.
    private void validateCanModify(ProjectMember requester, Task task) {
        boolean isAssignee = task.getAssignee() != null
                && task.getAssignee().getId().equals(requester.getUser().getId());
        if (!authorizationService.isManager(requester) && !isAssignee) {
            throw new ApiException(ErrorCode.TASK_PERMISSION_DENIED);
        }
    }

    // 역할: 클라이언트가 보낸 버전이 현재 작업 버전과 일치하는지 검증합니다.
    private void validateVersion(Task task, Long requestedVersion) {
        if (!task.getVersion().equals(requestedVersion)) {
            throw new ApiException(ErrorCode.TASK_VERSION_CONFLICT);
        }
    }

    // 역할: 작업 설명이 있으면 앞뒤 공백을 제거해 저장 형식으로 정규화합니다.
    private String normalizeDescription(String description) {
        return description == null ? null : description.trim();
    }

    // 역할: 빈 검색어를 제거하고 유효한 검색어의 앞뒤 공백을 정리합니다.
    private String normalizeKeyword(String keyword) {
        return keyword == null || keyword.isBlank() ? null : keyword.trim();
    }

    // 역할: 페이지 번호와 페이지 크기가 허용 범위에 있는지 검증합니다.
    private void validatePagination(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new ApiException(ErrorCode.INVALID_PAGINATION);
        }
    }
}
