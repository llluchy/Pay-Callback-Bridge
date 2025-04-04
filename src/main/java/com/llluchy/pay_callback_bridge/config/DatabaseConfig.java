package com.llluchy.pay_callback_bridge.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

/**
 * 数据库配置类
 * 仅负责数据库相关的配置
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.llluchy.pay_callback_bridge.repository")
@EntityScan(basePackages = "com.llluchy.pay_callback_bridge.entity")
public class DatabaseConfig {
    // 这里不需要任何代码，注解已经完成了配置工作
} 