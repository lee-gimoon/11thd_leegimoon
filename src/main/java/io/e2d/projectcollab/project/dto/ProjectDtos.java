package io.e2d.projectcollab.project.dto;

import io.e2d.projectcollab.project.domain.Project;
import io.e2d.projectcollab.project.domain.ProjectMember;
import io.e2d.projectcollab.project.domain.ProjectRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class ProjectDtos {

    private ProjectDtos() {
    }

    public record CreateProjectRequest(
            @NotBlank(message = "프로젝트 이름은 필수입니다.")
            @Size(max = 100, message = "프로젝트 이름은 100자 이하여야 합니다.")
            String name,

            @Size(max = 1000, message = "프로젝트 설명은 1000자 이하여야 합니다.")
            String description
    ) {
    }

    public record UpdateProjectRequest(
            @NotBlank(message = "프로젝트 이름은 필수입니다.")
            @Size(max = 100, message = "프로젝트 이름은 100자 이하여야 합니다.")
            String name,

            @Size(max = 1000, message = "프로젝트 설명은 1000자 이하여야 합니다.")
            String description
    ) {
    }

    public record ProjectResponse(
            Long id,
            String name,
            String description,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        public static ProjectResponse from(Project project) {
            return new ProjectResponse(
                    project.getId(),
                    project.getName(),
                    project.getDescription(),
                    project.getCreatedAt(),
                    project.getUpdatedAt()
            );
        }
    }

    public record AddProjectMemberRequest(
            @NotNull(message = "사용자 ID는 필수입니다.")
            Long userId,

            @NotNull(message = "역할은 필수입니다.")
            ProjectRole role
    ) {
    }

    public record ChangeMemberRoleRequest(
            @NotNull(message = "역할은 필수입니다.")
            ProjectRole role
    ) {
    }

    public record ProjectMemberResponse(
            Long id,
            Long projectId,
            Long userId,
            String userName,
            String userEmail,
            ProjectRole role,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        public static ProjectMemberResponse from(ProjectMember member) {
            return new ProjectMemberResponse(
                    member.getId(),
                    member.getProject().getId(),
                    member.getUser().getId(),
                    member.getUser().getName(),
                    member.getUser().getEmail(),
                    member.getRole(),
                    member.getCreatedAt(),
                    member.getUpdatedAt()
            );
        }
    }
}
