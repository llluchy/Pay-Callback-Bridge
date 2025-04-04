package com.llluchy.pay_callback_bridge.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.llluchy.pay_callback_bridge.dto.response.CallbackDetailDTO;
import com.llluchy.pay_callback_bridge.dto.request.SetResponseRequest;
import com.llluchy.pay_callback_bridge.entity.WechatPayCallback;
import com.llluchy.pay_callback_bridge.repository.WechatPayCallbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class WechatPayCallbackService {

    @Autowired
    private WechatPayCallbackRepository callbackRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 处理微信支付回调请求
     * @param headers 请求头
     * @param body 请求体
     * @return 处理后的回调对象
     */
    @Transactional
    public WechatPayCallback processCallback(Map<String, String> headers, String body) {
        try {
            // 解析回调ID和事件类型
            JsonNode rootNode = objectMapper.readTree(body);
            String callbackId = rootNode.path("id").asText();
            
            // 查找是否有相同的回调
            Optional<WechatPayCallback> existingCallback = callbackRepository.findByCallbackId(callbackId);
            
            if (existingCallback.isPresent()) {
                // 如果存在相同回调，更新计数和接收时间
                WechatPayCallback callback = existingCallback.get();
                callback.setReceivedCount(callback.getReceivedCount() + 1);
                callback.setLastReceiveTime(LocalDateTime.now());
                
                // 如果是第7次接收且未设置响应，自动设置成功响应
                if (callback.getReceivedCount() >= 7 && !callback.getIsResponseSet()) {
                    callback.setIsResponseSet(true);
                    // callback.setResponseBody();
                    callback.setResponseHttpStatus(200);
                    callback.setResponseTime(LocalDateTime.now());
                    callback.setStatus(WechatPayCallback.CallbackStatus.AUTO_PROCESSED);
                }
                
                return callbackRepository.save(callback);
            } else {
                // 如果是新的回调，创建新记录
                WechatPayCallback newCallback = new WechatPayCallback();
                newCallback.setCallbackId(callbackId);
                newCallback.setHeaders(objectMapper.writeValueAsString(headers));
                newCallback.setBody(body);
                newCallback.setReceiveTime(LocalDateTime.now());
                newCallback.setLastReceiveTime(LocalDateTime.now());
                newCallback.setReceivedCount(1);
                // 设置初始状态为PENDING
                newCallback.setStatus(WechatPayCallback.CallbackStatus.PENDING);
                
                return callbackRepository.save(newCallback);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("处理回调失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取回调详情
     * @param requestId 请求ID
     * @return 回调详情
     */
    public CallbackDetailDTO getCallbackDetail(String id) {
        WechatPayCallback callback = callbackRepository.findByCallbackId(id)
            .orElseThrow(() -> new RuntimeException("回调不存在: " + id));
        
        return convertToCallbackDetail(callback);
    }
    
    /**
     * 设置回调响应
     * @param request 设置响应请求
     * @return 更新后的回调对象
     */
    @Transactional
    public WechatPayCallback setCallbackResponse(SetResponseRequest request) {
        WechatPayCallback callback = callbackRepository.findByCallbackId(request.getCallbackId())
            .orElseThrow(() -> new RuntimeException("回调不存在: " + request.getCallbackId()));
        
        callback.setIsResponseSet(true);
        callback.setResponseBody(request.getResponseBody());
        callback.setResponseHttpStatus(request.getHttpStatus());
        callback.setResponseTime(LocalDateTime.now());
        
        return callbackRepository.save(callback);
    }
    
    /**
     * 检查回调是否已设置响应，用于决定返回什么给微信
     * @param callbackId 回调ID
     * @return 响应内容的Optional包装，如果未设置则为空
     */
    public Optional<Map<String, Object>> getCallbackResponse(String callbackId) {
        return callbackRepository.findByCallbackId(callbackId)
            .filter(WechatPayCallback::getIsResponseSet)
            .map(callback -> {
                Map<String, Object> response = new HashMap<>();
                response.put("body", callback.getResponseBody());
                response.put("status", callback.getResponseHttpStatus());
                return response;
            });
    }

    /**
     * 更新回调状态
     * @param callbackId 回调ID
     * @param status 新状态
     * @return 更新后的回调
     */
    @Transactional
    public WechatPayCallback updateCallbackStatus(String callbackId, WechatPayCallback.CallbackStatus status) {
        WechatPayCallback callback = callbackRepository.findByCallbackId(callbackId)
            .orElseThrow(() -> new RuntimeException("回调不存在: " + callbackId));
        
        callback.setStatus(status);
        if (status != WechatPayCallback.CallbackStatus.PENDING) {
            callback.setResponseTime(LocalDateTime.now());
        }
        
        return callbackRepository.save(callback);
    }

    // 辅助方法，转换实体为详情DTO
    private CallbackDetailDTO convertToCallbackDetail(WechatPayCallback callback) {
        try {
            CallbackDetailDTO dto = new CallbackDetailDTO();
            dto.setCallbackId(callback.getCallbackId());
            dto.setBody(callback.getBody());
            dto.setReceiveTime(callback.getReceiveTime());
            dto.setLastReceiveTime(callback.getLastReceiveTime());
            dto.setResponseTime(callback.getResponseTime());
            dto.setReceivedCount(callback.getReceivedCount());
            dto.setResponseSet(callback.getIsResponseSet());
            dto.setResponseBody(callback.getResponseBody());
            dto.setResponseHttpStatus(callback.getResponseHttpStatus());
            dto.setStatus(callback.getStatus());
            
            // 解析保存为JSON的headers
            TypeReference<Map<String, String>> typeRef = new TypeReference<Map<String, String>>() {};
            dto.setHeaders(objectMapper.readValue(callback.getHeaders(), typeRef));
            
            return dto;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("转换回调详情失败: " + e.getMessage(), e);
        }
    }
} 