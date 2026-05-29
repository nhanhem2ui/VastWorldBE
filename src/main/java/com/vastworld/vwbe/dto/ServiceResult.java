package com.vastworld.vwbe.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    public static <T> ServiceResult<T> success(String message, T data) {
        ServiceResult<T> result = new ServiceResult<>();
        result.setSuccess(true);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    public static <T> ServiceResult<T> success(String message) {
        ServiceResult<T> result = new ServiceResult<>();
        result.setSuccess(true);
        result.setMessage(message);
        result.setData(null);
        return result;
    }

    public static <T> ServiceResult<T> failure(String message) {
        ServiceResult<T> result = new ServiceResult<>();
        result.setSuccess(false);
        result.setMessage(message);
        return result;
    }

    public static <T> ServiceResult<T> failure(String message, Throwable ex) {
        ServiceResult<T> result = new ServiceResult<>();
        result.setSuccess(false);
        result.setMessage(message);
        result.setErrors(Collections.singletonList(ex.getMessage()));
        return result;
    }
}