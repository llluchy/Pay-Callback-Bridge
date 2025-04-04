package com.llluchy.pay_callback_bridge.service;

import com.llluchy.pay_callback_bridge.entity.WechatPayCallback;
import com.llluchy.pay_callback_bridge.repository.WechatPayCallbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务服务
 * 负责管理所有定期执行的后台任务
 */
@Service
public class ScheduledTaskService {
    
    @Autowired
    private WechatPayCallbackRepository callbackRepository;
    
    /**
     * 定时清理24小时前的回调数据
     * 每小时执行一次
     */
    @Scheduled(fixedRate = 3600000) // 1小时 = 3600000毫秒
    @Transactional
    public void cleanupOldCallbacks() {
        LocalDateTime oneDayAgo = LocalDateTime.now().minusHours(24);
        
        // 使用JPA查询过期数据
        List<WechatPayCallback> expiredCallbacks = callbackRepository.findCallbacksOlderThan(oneDayAgo);
        
        if (!expiredCallbacks.isEmpty()) {
            int count = expiredCallbacks.size();
            callbackRepository.deleteAll(expiredCallbacks);
            System.out.println("已清理 " + count + " 条过期的回调数据");
        }
    }
    
    /**
     * 统计任务
     * 每天凌晨1点执行
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void dailyStatistics() {
        // 这里可以添加每日统计任务
        // 例如：统计每日处理的回调数量等
        System.out.println("执行每日统计任务: " + LocalDateTime.now());
    }
} 