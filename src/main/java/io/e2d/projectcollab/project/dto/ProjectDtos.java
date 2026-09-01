// 목적: 프로젝트와 멤버 API에서 사용하는 요청·응답 데이터를 정의하기 위해 만들어진 파일입니다.
// 역할: 프로젝트 및 멤버 관리에 필요한 DTO를 한곳에 제공합니다.
package io.e2d.projectcollab.project.dto;

import io.e2d.projectcollab.project.domain.Project;
import io.e2d.projectcollab.project.domain.ProjectMember;
import io.e2d.projectcollab.project.domain.ProjectRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

// 역할: 프로젝트와 프로젝트 멤버 기능에 필요한 DTO 타입을 묶어서 관리합니다.
public final class ProjectDtos {

    // 역할: 인스턴스 생성을 막아 DTO 컨테이너로만 사용되게 합니다.
    private ProjectDtos() {
    }

    // 역할: 새 프로젝트 생성에 필요한 이름과 설명을 전달합니다.
    public record CreateProjectRequest(
            @NotBlank(message = "프로젝트 이름은 필수입니다.")
            @Size(max = 100, message = "프로젝트 이름은 100자 이하여야 합니다.")
            String name,

            @Size(max = 1000, message = "프로젝트 설명은 1000자 이하여야 합니다.")
            String description
    ) {
    }

    // 역할: 프로젝트 수정에 필요한 이름과 설명을 전달합니다.
    public record UpdateProjectRequest(
            @NotBlank(message = "프로젝트 이름은 필수입니다.")
            @Size(max = 100, message = "프로젝트 이름은 100자 이하여야 합니다.")
            String name,

            @Size(max = 1000, message = "프로젝트 설명은 1000자 이하여야 합니다.")
            String description
    ) {
    }

    // 역할: 프로젝트의 공개 가능한 정보를 API 응답으로 전달합니다.
    public record ProjectResponse(
            Long id,
            String name,
            String description,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        // 역할: 프로젝트 엔티티를 프로젝트 응답 DTO로 변환합니다.
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

    // 역할: 프로젝트에 추가할 사용자와 부여할 역할을 전달합니다.
    public record AddProjectMemberRequest(
            @NotNull(message = "사용자 ID는 필수입니다.")
            Long userId,

            @NotNull(message = "역할은 필수입니다.")
            ProjectRole role
    ) {
    }

    // 역할: 프로젝트 멤버에게 새로 부여할 역할을 전달합니다.
    public record ChangeMemberRoleRequest(
            @NotNull(message = "역할은 필수입니다.")
            ProjectRole role
    ) {
    }

    // 역할: 프로젝트 멤버와 사용자 및 역할 정보를 API 응답으로 전달합니다.
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
        // 역할: 프로젝트 멤버 엔티티를 멤버 응답 DTO로 변환합니다.
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
