// 목적: 작업 API에서 사용하는 요청·응답 및 페이지 데이터를 정의하기 위해 만들어진 파일입니다.
// 역할: 작업 생성·수정 요청과 단건·목록 응답 DTO를 한곳에 제공합니다.
package io.e2d.projectcollab.task.dto;

import io.e2d.projectcollab.task.domain.Task;
import io.e2d.projectcollab.task.domain.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

// 역할: 작업 기능에 필요한 DTO 타입을 묶어서 관리합니다.
public final class TaskDtos {

    // 역할: 인스턴스 생성을 막아 DTO 컨테이너로만 사용되게 합니다.
    private TaskDtos() {
    }

    // 역할: 새 작업 생성에 필요한 내용, 상태와 담당자 정보를 전달합니다.
    public record CreateTaskRequest(
            @NotBlank(message = "작업 제목은 필수입니다.")
            @Size(max = 200, message = "작업 제목은 200자 이하여야 합니다.")
            String title,

            @Size(max = 2000, message = "작업 설명은 2000자 이하여야 합니다.")
            String description,

            TaskStatus status,

            Long assigneeId
    ) {
    }

    // 역할: 작업 수정에 필요한 내용, 상태, 담당자와 현재 버전을 전달합니다.
    public record UpdateTaskRequest(
            @NotBlank(message = "작업 제목은 필수입니다.")
            @Size(max = 200, message = "작업 제목은 200자 이하여야 합니다.")
            String title,

            @Size(max = 2000, message = "작업 설명은 2000자 이하여야 합니다.")
            String description,

            @NotNull(message = "작업 상태는 필수입니다.")
            TaskStatus status,

            Long assigneeId,

            @NotNull(message = "작업 버전은 필수입니다.")
            @PositiveOrZero(message = "작업 버전은 0 이상이어야 합니다.")
            Long version
    ) {
    }

    // 역할: 작업과 담당자의 공개 가능한 정보를 API 응답으로 전달합니다.
    public record TaskResponse(
            Long id,
            Long projectId,
            String title,
            String description,
            TaskStatus status,
            Long assigneeId,
            String assigneeName,
            Long version,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        // 역할: 작업 엔티티를 담당자 정보가 포함된 작업 응답 DTO로 변환합니다.
        public static TaskResponse from(Task task) {
            Long assigneeId = task.getAssignee() == null ? null : task.getAssignee().getId();
            String assigneeName = task.getAssignee() == null
                    ? null
                    : task.getAssignee().getName();

            return new TaskResponse(
                    task.getId(),
                    task.getProject().getId(),
                    task.getTitle(),
                    task.getDescription(),
                    task.getStatus(),
                    assigneeId,
                    assigneeName,
                    task.getVersion(),
                    task.getCreatedAt(),
                    task.getUpdatedAt()
            );
        }
    }

    // 역할: 작업 목록과 페이지 탐색 정보를 API 응답으로 전달합니다.
    public record TaskPageResponse(
            List<TaskResponse> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean first,
            boolean last,
            boolean hasNext,
            boolean hasPrevious
    ) {
        // 역할: 작업 페이지를 작업 응답 목록과 페이지 메타데이터로 변환합니다.
        public static TaskPageResponse from(Page<Task> taskPage) {
            return new TaskPageResponse(
                    taskPage.getContent().stream()
                            .map(TaskResponse::from)
                            .toList(),
                    taskPage.getNumber(),
                    taskPage.getSize(),
                    taskPage.getTotalElements(),
                    taskPage.getTotalPages(),
                    taskPage.isFirst(),
                    taskPage.isLast(),
                    taskPage.hasNext(),
                    taskPage.hasPrevious()
            );
        }
    }
}
