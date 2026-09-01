// 목적: API에서 발생할 수 있는 오류를 한곳에서 정의하기 위해 만들어진 파일입니다.
// 역할: 오류별 HTTP 상태와 사용자 메시지를 표준화합니다.
package io.e2d.projectcollab.common.exception;

import org.springframework.http.HttpStatus;

// 역할: 애플리케이션의 오류 유형과 각 오류의 HTTP 응답 정보를 정의합니다.
public enum ErrorCode {
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않습니다."),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "요청값 검증에 실패했습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "프로젝트를 찾을 수 없습니다."),
    PROJECT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "프로젝트에 접근할 권한이 없습니다."),
    PROJECT_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "요청한 프로젝트 작업을 수행할 권한이 없습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "프로젝트 멤버를 찾을 수 없습니다."),
    MEMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 프로젝트에 속한 사용자입니다."),
    LAST_OWNER_REQUIRED(HttpStatus.CONFLICT, "프로젝트에는 최소 한 명의 OWNER가 필요합니다."),
    TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "작업을 찾을 수 없습니다."),
    TASK_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "작업을 수정하거나 삭제할 권한이 없습니다."),
    ASSIGNEE_NOT_PROJECT_MEMBER(HttpStatus.BAD_REQUEST, "작업 담당자는 프로젝트 멤버여야 합니다."),
    TASK_VERSION_CONFLICT(HttpStatus.CONFLICT, "다른 사용자가 작업을 먼저 수정했습니다. 최신 내용을 다시 조회해 주세요."),
    INVALID_PAGINATION(HttpStatus.BAD_REQUEST, "페이지는 0 이상, 페이지 크기는 1 이상 100 이하여야 합니다."),
    DATA_INTEGRITY_VIOLATION(HttpStatus.CONFLICT, "데이터 제약 조건을 위반했습니다.");

    private final HttpStatus status;
    private final String message;

    // 역할: 오류 코드에 HTTP 상태와 사용자 메시지를 연결합니다.
    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    // 역할: 오류에 대응하는 HTTP 상태를 반환합니다.
    public HttpStatus getStatus() {
        return status;
    }

    // 역할: 오류를 설명하는 사용자 메시지를 반환합니다.
    public String getMessage() {
        return message;
    }
}
