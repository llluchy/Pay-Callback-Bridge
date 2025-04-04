package com.llluchy.pay_callback_bridge.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CallbackInfoDTO {
    private String requestId;
    private String callbackId;
    private LocalDateTime receiveTime;
    private int receivedCount;
} 