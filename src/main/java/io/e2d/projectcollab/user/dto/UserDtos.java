// 목적: 사용자 API에서 사용하는 요청과 응답 데이터를 정의하기 위해 만들어진 파일입니다.
// 역할: 사용자 등록 요청과 사용자 조회 응답 DTO를 한곳에 제공합니다.
package io.e2d.projectcollab.user.dto;

import io.e2d.projectcollab.user.domain.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

// 역할: 사용자 기능에 필요한 DTO 타입을 묶어서 관리합니다.
public final class UserDtos {

    // 역할: 인스턴스 생성을 막아 DTO 컨테이너로만 사용되게 합니다.
    private UserDtos() {
    }

    // 역할: 새 사용자 등록에 필요한 이름과 이메일을 전달합니다.
    public record CreateUserRequest(
            @NotBlank(message = "이름은 필수입니다.")
            @Size(max = 100, message = "이름은 100자 이하여야 합니다.")
            String name,

            @NotBlank(message = "이메일은 필수입니다.")
            @Email(message = "이메일 형식이 올바르지 않습니다.")
            @Size(max = 255, message = "이메일은 255자 이하여야 합니다.")
            String email
    ) {
    }

    // 역할: 사용자 엔티티의 공개 가능한 정보를 API 응답으로 전달합니다.
    public record UserResponse(
            Long id,
            String name,
            String email,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        // 역할: 사용자 엔티티를 사용자 응답 DTO로 변환합니다.
        public static UserResponse from(User user) {
            return new UserResponse(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getCreatedAt(),
                    user.getUpdatedAt()
            );
        }
    }
}
