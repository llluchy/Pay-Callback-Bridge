package com.llluchy.pay_callback_bridge.dto.response;

import com.llluchy.pay_callback_bridge.entity.WechatPayCallback;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class CallbackDetailDTO {
    private String requestId;
    private String callbackId;
    private Map<String, String> headers;
    private String body;
    private String eventType;
    private LocalDateTime receiveTime;
    private LocalDateTime lastReceiveTime;
    private LocalDateTime responseTime;
    private int receivedCount;
    private boolean isResponseSet;
    private String responseBody;
    private Integer responseHttpStatus;
    private WechatPayCallback.CallbackStatus status;
} 