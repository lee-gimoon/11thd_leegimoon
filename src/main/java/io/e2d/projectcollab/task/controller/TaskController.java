package io.e2d.projectcollab.task.controller;

import io.e2d.projectcollab.task.dto.TaskDtos.CreateTaskRequest;
import io.e2d.projectcollab.task.dto.TaskDtos.TaskPageResponse;
import io.e2d.projectcollab.task.dto.TaskDtos.TaskResponse;
import io.e2d.projectcollab.task.dto.TaskDtos.UpdateTaskRequest;
import io.e2d.projectcollab.task.domain.TaskStatus;
import io.e2d.projectcollab.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static io.e2d.projectcollab.common.web.RequesterHeaders.REQUESTER_ID;

@Tag(name = "Tasks", description = "프로젝트 작업 기본 관리")
@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Operation(summary = "작업 생성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(
            @RequestHeader(REQUESTER_ID) Long requesterId,
            @PathVariable Long projectId,
            @Valid @RequestBody CreateTaskRequest request
    ) {
        return taskService.create(requesterId, projectId, request);
    }

    @Operation(
            summary = "작업 목록 조회",
            description = "제목·설명 검색, 상태 필터와 0부터 시작하는 페이지 조회를 지원합니다."
    )
    @GetMapping
    public TaskPageResponse getAll(
            @RequestHeader(REQUESTER_ID) Long requesterId,
            @PathVariable Long projectId,
            @Parameter(description = "작업 제목과 설명에서 찾을 검색어")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "TODO, IN_PROGRESS, DONE 중 하나")
            @RequestParam(required = false) TaskStatus status,
            @Parameter(description = "0부터 시작하는 페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지당 작업 수(1~100)", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        return taskService.getAll(requesterId, projectId, keyword, status, page, size);
    }

    @Operation(summary = "작업 조회")
    @GetMapping("/{taskId}")
    public TaskResponse get(
            @RequestHeader(REQUESTER_ID) Long requesterId,
            @PathVariable Long projectId,
            @PathVariable Long taskId
    ) {
        return taskService.get(requesterId, projectId, taskId);
    }

    @Operation(summary = "작업 수정")
    @PutMapping("/{taskId}")
    public TaskResponse update(
            @RequestHeader(REQUESTER_ID) Long requesterId,
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskRequest request
    ) {
        return taskService.update(requesterId, projectId, taskId, request);
    }

    @Operation(summary = "작업 삭제")
    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestHeader(REQUESTER_ID) Long requesterId,
            @PathVariable Long projectId,
            @PathVariable Long taskId
    ) {
        taskService.delete(requesterId, projectId, taskId);
    }
}
