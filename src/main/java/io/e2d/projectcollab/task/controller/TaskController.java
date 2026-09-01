// 목적: 프로젝트 작업의 생성·검색·조회·수정·삭제 HTTP 요청을 처리하기 위해 만들어진 파일입니다.
// 역할: 작업 API 요청을 서비스 계층에 전달하고 결과를 반환합니다.
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

// 역할: 프로젝트 작업 기본 관리 및 검색 REST API를 제공합니다.
@Tag(name = "Tasks", description = "프로젝트 작업 기본 관리")
@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
public class TaskController {

    private final TaskService taskService;

    // 역할: 작업 서비스 의존성을 주입받아 컨트롤러를 생성합니다.
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // 역할: 프로젝트에 새 작업을 생성합니다.
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

    // 역할: 검색어, 상태, 페이지 조건으로 프로젝트 작업 목록을 조회합니다.
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

    // 역할: 프로젝트에 속한 지정 작업을 조회합니다.
    @Operation(summary = "작업 조회")
    @GetMapping("/{taskId}")
    public TaskResponse get(
            @RequestHeader(REQUESTER_ID) Long requesterId,
            @PathVariable Long projectId,
            @PathVariable Long taskId
    ) {
        return taskService.get(requesterId, projectId, taskId);
    }

    // 역할: 권한과 버전을 확인한 뒤 작업 정보를 수정합니다.
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

    // 역할: 수정 권한을 확인한 뒤 작업을 삭제합니다.
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
