package com.llluchy.pay_callback_bridge.dto.request;

import lombok.Data;

@Data
public class SetResponseRequest {
    private String callbackId;
    private String responseBody;
    private Integer httpStatus;
} 