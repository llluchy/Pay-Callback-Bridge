package com.llluchy.pay_callback_bridge.controller;

import com.llluchy.pay_callback_bridge.dto.request.SetResponseRequest;
import com.llluchy.pay_callback_bridge.dto.response.ApiResponse;
import com.llluchy.pay_callback_bridge.dto.response.CallbackDetailDTO;
import com.llluchy.pay_callback_bridge.entity.WechatPayCallback;
import com.llluchy.pay_callback_bridge.service.WechatPayCallbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户回调管理控制器
 * 提供给用户查询和设置回调的接口
 */
@RestController
@RequestMapping("/api/pay-callback/user")
public class UserCallbackController {

    @Autowired
    private WechatPayCallbackService callbackService;

    /**
     * 用户处理回调接口
     * 通过一个请求同时查询回调详情并设置响应
     * 
     * @param id 回调ID
     * @param status 要设置的HTTP状态码
     * @param responseBody 要设置的响应内容
     * @return 处理结果
     */
    @PostMapping("/handle-callback/{id}")
    public ApiResponse<Map<String, Object>> handleCallback(
            @PathVariable String id,
            @RequestParam(defaultValue = "200") Integer status,
            @RequestParam(required = false) String responseBody) {
        
        try {
            // 1. 获取回调详情
            CallbackDetailDTO detail = callbackService.getCallbackDetail(id);
            Map<String, Object> result = new HashMap<>();
            
            // 2. 检查状态
            if (detail.getStatus() != WechatPayCallback.CallbackStatus.PENDING) {
                // 已响应过微信
                result.put("status", "已响应");
                result.put("responseHttpStatus", detail.getResponseHttpStatus());
                result.put("responseBody", detail.getResponseBody());
                result.put("responseTime", detail.getResponseTime());
                return ApiResponse.success(result);
            }
            
            // 3. 设置响应
            SetResponseRequest setRequest = new SetResponseRequest();
            setRequest.setCallbackId(id);
            setRequest.setHttpStatus(status);
            setRequest.setResponseBody(responseBody != null ? responseBody : "");
            
            WechatPayCallback callback = callbackService.setCallbackResponse(setRequest);
            
            // 4. 返回结果
            result.put("status", "已设置");
            result.put("currentCount", callback.getReceivedCount());
            result.put("callbackStatus", callback.getStatus().toString());
            result.put("message", "已设置返回信息，等待下一次微信回调，当前次数为第" + callback.getReceivedCount() + "次");
            
            return ApiResponse.success(result);
            
        } catch (Exception e) {
            return ApiResponse.error(500, "处理回调失败: " + e.getMessage());
        }
    }
} 