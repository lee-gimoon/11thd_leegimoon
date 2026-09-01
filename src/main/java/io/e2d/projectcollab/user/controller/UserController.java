// 목적: 사용자 등록과 조회 HTTP 요청을 처리하기 위해 만들어진 파일입니다.
// 역할: 사용자 API 요청을 서비스 계층에 전달하고 응답을 반환합니다.
package io.e2d.projectcollab.user.controller;

import io.e2d.projectcollab.user.dto.UserDtos.CreateUserRequest;
import io.e2d.projectcollab.user.dto.UserDtos.UserResponse;
import io.e2d.projectcollab.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// 역할: 사용자 생성 및 단건 조회 REST API를 제공합니다.
@Tag(name = "Users", description = "사용자 등록 및 조회")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    // 역할: 사용자 서비스 의존성을 주입받아 컨트롤러를 생성합니다.
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 역할: 입력값을 검증하고 새 사용자를 등록합니다.
    @Operation(summary = "사용자 등록")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        return userService.create(request);
    }

    // 역할: 사용자 ID에 해당하는 사용자 정보를 조회합니다.
    @Operation(summary = "사용자 조회")
    @GetMapping("/{userId}")
    public UserResponse get(@PathVariable Long userId) {
        return userService.get(userId);
    }
}
