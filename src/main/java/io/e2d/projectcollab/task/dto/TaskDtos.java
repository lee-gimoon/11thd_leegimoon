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

public final class TaskDtos {

    private TaskDtos() {
    }

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
