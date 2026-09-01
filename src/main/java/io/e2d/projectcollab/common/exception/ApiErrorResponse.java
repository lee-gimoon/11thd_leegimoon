// 목적: API 오류 응답 형식을 일관되게 표현하기 위해 만들어진 파일입니다.
// 역할: 오류 코드, 메시지, 상태, 발생 시각, 요청 경로와 필드 오류를 전달합니다.
package io.e2d.projectcollab.common.exception;

import java.time.OffsetDateTime;
import java.util.Map;

// 역할: 클라이언트에 반환할 표준 API 오류 응답 데이터를 보관합니다.
public record ApiErrorResponse(
        String code,
        String message,
        int status,
        OffsetDateTime timestamp,
        String path,
        Map<String, String> fieldErrors
) {
    // 역할: 오류 코드와 요청 정보를 표준 API 오류 응답으로 변환합니다.
    public static ApiErrorResponse of(
            ErrorCode errorCode,
            String path,
            Map<String, String> fieldErrors
    ) {
        return new ApiErrorResponse(
                errorCode.name(),
                errorCode.getMessage(),
                errorCode.getStatus().value(),
                OffsetDateTime.now(),
                path,
                fieldErrors
        );
    }
}
