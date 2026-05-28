package com.vastworld.vwbe.common;

import com.vastworld.vwbe.dto.ServiceResult;
import org.springframework.http.HttpStatus;

public class Common {
    public static HttpStatus resolveStatus(ServiceResult<?> result, HttpStatus successStatus) {
        if (result.isSuccess()) {
            return successStatus;
        }

        String message = result.getMessage() == null ? "" : result.getMessage().toLowerCase();
        if (message.contains("not found")) {
            return HttpStatus.NOT_FOUND;
        }

        if (message.contains("already exists") || message.contains("invalid") || message.contains("required")) {
            return HttpStatus.BAD_REQUEST;
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
