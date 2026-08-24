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

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        String name = request.name().trim();
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        return UserResponse.from(userRepository.save(User.create(name, email)));
    }

    public UserResponse get(Long userId) {
        return UserResponse.from(getEntity(userId));
    }

    public User getEntity(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
    }
}
