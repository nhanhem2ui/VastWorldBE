package com.vastworld.vwbe.common;

import com.vastworld.vwbe.dto.ServiceResult;
import org.springframework.http.HttpStatus;

public class Common {
    public static HttpStatus resolveStatus(ServiceResult<?> result, HttpStatus successStatus) {
        if (result.isSuccess()) {
            return successStatus;
        }

        String message = result.getMessage() == null ? "" : result.getMessage().toLowerCase();
        if (message.contains("not found") || message.contains("not exists")) {
            return HttpStatus.NOT_FOUND;
        }

        if (message.contains("banned") || message.contains("forbidden")) {
            return HttpStatus.FORBIDDEN;
        }

        if (message.contains("credentials") || message.contains("not authenticated") || message.contains("unauthenticated")) {
            return HttpStatus.UNAUTHORIZED;
        }

        if (message.contains("conflict") || message.contains("already")
                || message.contains("duplicate")) {
            return HttpStatus.CONFLICT;
        }

        if (message.contains("validation") || message.contains("invalid") || message.contains("required")) {
            return HttpStatus.BAD_REQUEST;
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
