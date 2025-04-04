package com.llluchy.pay_callback_bridge.dto.response;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;
    
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "success", data);
    }
    
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(0, message, data);
    }
    
    public static ApiResponse<Void> success() {
        return new ApiResponse<>(0, "success", null);
    }
    
    public static ApiResponse<Map<String, Object>> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
} 