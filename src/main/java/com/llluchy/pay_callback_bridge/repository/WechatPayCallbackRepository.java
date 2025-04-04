package com.llluchy.pay_callback_bridge.repository;

import com.llluchy.pay_callback_bridge.entity.WechatPayCallback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WechatPayCallbackRepository extends JpaRepository<WechatPayCallback, Long> {

    @Query("SELECT w FROM WechatPayCallback w WHERE w.createdAt < :dateTime")
    List<WechatPayCallback> findCallbacksOlderThan(@Param("dateTime") LocalDateTime dateTime);
    
    Optional<WechatPayCallback> findByCallbackId(String callbackId);
} 