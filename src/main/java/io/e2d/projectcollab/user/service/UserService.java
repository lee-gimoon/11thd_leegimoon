// 목적: 사용자 등록과 조회 업무 규칙을 처리하기 위해 만들어진 파일입니다.
// 역할: 이메일 중복 검사, 사용자 저장, 사용자 조회를 수행합니다.
package io.e2d.projectcollab.user.service;

import io.e2d.projectcollab.common.exception.ApiException;
import io.e2d.projectcollab.common.exception.ErrorCode;
import io.e2d.projectcollab.user.domain.User;
import io.e2d.projectcollab.user.dto.UserDtos.CreateUserRequest;
import io.e2d.projectcollab.user.dto.UserDtos.UserResponse;
import io.e2d.projectcollab.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

// 역할: 사용자 도메인의 생성 및 조회 흐름을 담당합니다.
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    // 역할: 사용자 저장소 의존성을 주입받아 서비스를 생성합니다.
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 역할: 입력값을 정규화하고 이메일 중복을 검사한 뒤 새 사용자를 저장합니다.
    @Transactional
    public UserResponse create(CreateUserRequest request) {
        String name = request.name().trim();
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        return UserResponse.from(userRepository.save(User.create(name, email)));
    }

    // 역할: 사용자 ID로 사용자 정보를 조회해 응답 DTO로 반환합니다.
    public UserResponse get(Long userId) {
        return UserResponse.from(getEntity(userId));
    }

    // 역할: 사용자 ID로 엔티티를 조회하고 없으면 사용자 없음 예외를 발생시킵니다.
    public User getEntity(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
    }
}
