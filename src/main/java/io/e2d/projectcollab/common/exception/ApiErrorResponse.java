package io.e2d.projectcollab.common.exception;

import java.time.OffsetDateTime;
import java.util.Map;

public record ApiErrorResponse(
        String code,
        String message,
        int status,
        OffsetDateTime timestamp,
        String path,
        Map<String, String> fieldErrors
) {
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
