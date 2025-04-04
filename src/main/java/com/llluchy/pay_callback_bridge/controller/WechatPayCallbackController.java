package com.llluchy.pay_callback_bridge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.llluchy.pay_callback_bridge.entity.WechatPayCallback;
import com.llluchy.pay_callback_bridge.service.WechatPayCallbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 微信支付回调接收控制器
 * 仅负责接收微信支付的回调请求
 */
@RestController
@RequestMapping("/api/pay-callback/service")
public class WechatPayCallbackController {

    @Autowired
    private WechatPayCallbackService callbackService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 接收微信支付回调
     * @param body 请求体
     * @param headers 请求头
     * @return 响应对象
     */
    @SuppressWarnings("unchecked")
    @PostMapping("/callback")
    public ResponseEntity<String> receiveCallback(
            @RequestBody String body,
            @RequestHeader Map<String, String> headers) {
        
        try {
            // 从请求中提取回调ID
            Map<String, Object> bodyMap = objectMapper.readValue(body, Map.class);
            String callbackId = (String) bodyMap.get("id");
            
            // 检查是否有预设的响应
            if (callbackId != null) {
                var responseOpt = callbackService.getCallbackResponse(callbackId);
                if (responseOpt.isPresent()) {
                    // 如果有预设响应，直接返回
                    Map<String, Object> response = responseOpt.get();
                    if (response.get("status") != null) {
                        String responseBody = (String) response.get("body");
                        int status = (int) response.get("status");
                        
                        // 更新回调状态为RESPONDED
                        callbackService.updateCallbackStatus(callbackId, WechatPayCallback.CallbackStatus.PROCESSED);
                        
                        return ResponseEntity.status(status).body(responseBody);
                    }
                }
            }
            
            // 如果没有预设响应，处理回调
            WechatPayCallback callback = callbackService.processCallback(headers, body);
            
            // 如果是第7次回调，返回成功但不带任何信息，并更新状态为RESPONDED
            if (callback.getReceivedCount() >= 7) {
                if (callback.getStatus() == WechatPayCallback.CallbackStatus.AUTO_PROCESSED) {
                    callbackService.updateCallbackStatus(callback.getCallbackId(), WechatPayCallback.CallbackStatus.AUTO_PROCESSED);
                }
                return ResponseEntity.ok().body("");
            } else {
                // 否则返回错误状态并且返回错误信息
                // {  
                //     "code": "FAIL",
                //     "message": "失败"
                // }
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(objectMapper.writeValueAsString(Map.of("code", "FAIL", "message", "没有用户对该回调进行回应")));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("不作回应");
        }
    }
} 