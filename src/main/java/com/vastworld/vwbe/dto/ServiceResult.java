package com.vastworld.vwbe.dto;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceResult<T> {

    private boolean success;
    private String message;
    private T data;
    private List<String> errors;
    private Integer statusCode;

    public static <T> ServiceResult<T> success(String message, T data, HttpStatus status) {
        ServiceResult<T> result = new ServiceResult<>();
        result.setSuccess(true);
        result.setMessage(message);
        result.setData(data);
        result.setStatusCode(status.value());
        return result;
    }

    public static <T> ServiceResult<T> success(String message, HttpStatus status) {
        return success(message, null, status);
    }

    public static <T> ServiceResult<T> failure(String message, @Nullable HttpStatus status) {
        ServiceResult<T> result = new ServiceResult<>();
        result.setSuccess(false);
        result.setMessage(message);
        if (status != null) {
            result.setStatusCode(status.value());
        }
        return result;
    }

    public static <T> ServiceResult<T> failure(String message, Throwable ex, HttpStatus status) {
        ServiceResult<T> result = failure(message, status);
        result.setErrors(Collections.singletonList(ex.getMessage()));
        return result;
    }

    @Deprecated
    public static <T> ServiceResult<T> failure(String message) {
        return failure(message, (HttpStatus) null);
    }

    @Deprecated
    public static <T> ServiceResult<T> failure(String message, Throwable ex) {
        return failure(message, ex, null);
    }
}