package com.llluchy.pay_callback_bridge.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "wechat_pay_callback")
public class WechatPayCallback {
    
    // 主键ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 微信回调ID
    @Column(name = "callback_id", nullable = false, length = 64)
    private String callbackId;

    // 完整的HTTP请求头(JSON格式)
    @Column(name = "headers", nullable = false, columnDefinition = "TEXT")
    private String headers;
    
    // 完整的加密请求体(JSON格式)
    @Column(name = "body", nullable = false, columnDefinition = "LONGTEXT")
    private String body;

    // 首次接收时间
    @Column(name = "receive_time", nullable = false)
    private LocalDateTime receiveTime;
    
    // 最后接收时间
    @Column(name = "last_receive_time", nullable = false)
    private LocalDateTime lastReceiveTime;
    
    // 接收次数
    @Column(name = "received_count", nullable = false)
    private Integer receivedCount = 1;
    
    // 是否已设置响应内容
    @Column(name = "is_response_set", nullable = false)
    private Boolean isResponseSet = false;
    
    // 响应内容
    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;
    
    // 响应HTTP状态码
    @Column(name = "response_http_status")
    private Integer responseHttpStatus;
    
    // 回应微信的时间
    @Column(name = "response_time")
    private LocalDateTime responseTime;
    
    // 状态:PENDING(待处理)/PROCESSED(已处理)/AUTO_PROCESSED(自动处理)
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private CallbackStatus status = CallbackStatus.PENDING;
    
    // 创建时间
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // 更新时间
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // 回调状态枚举
    public enum CallbackStatus {
        PENDING,           // 待处理
        PROCESSED,         // 已处理
        AUTO_PROCESSED     // 自动处理
    }
} 