// 목적: 애플리케이션의 업무 오류를 예외로 전달하기 위해 만들어진 파일입니다.
// 역할: 발생한 오류 코드를 런타임 예외와 함께 보관합니다.
package io.e2d.projectcollab.common.exception;

// 역할: 서비스 계층의 업무 오류와 대응하는 오류 코드를 전달합니다.
public class ApiException extends RuntimeException {

    private final ErrorCode errorCode;

    // 역할: 지정된 오류 코드의 메시지를 가진 API 예외를 생성합니다.
    public ApiException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    // 역할: 이 예외에 연결된 오류 코드를 반환합니다.
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
