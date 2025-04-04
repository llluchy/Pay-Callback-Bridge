CREATE TABLE IF NOT EXISTS `wechat_pay_callback` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `callback_id` varchar(64) NOT NULL COMMENT '微信回调ID，如EV-20180225xxx',
  `headers` text NOT NULL COMMENT '完整的HTTP请求头(JSON格式)',
  `body` longtext NOT NULL COMMENT '完整的加密请求体(JSON格式)',
  `receive_time` datetime NOT NULL COMMENT '首次接收时间',
  `last_receive_time` datetime NOT NULL COMMENT '最后接收时间',
  `received_count` int(11) NOT NULL DEFAULT '1' COMMENT '接收次数',
  `is_response_set` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已设置响应内容',
  `response_body` text DEFAULT NULL COMMENT '响应内容',
  `response_http_status` int(11) DEFAULT NULL COMMENT '响应HTTP状态码',
  `response_time` datetime DEFAULT NULL COMMENT '回应微信的时间',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态:PENDING(待处理)/PROCESSED(已处理)/AUTO_PROCESSED(自动处理)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_callback_id` (`callback_id`),
  KEY `idx_route_param` (`route_param`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='微信支付回调请求记录表';

-- 创建定时清理事件
DELIMITER //
CREATE EVENT IF NOT EXISTS `cleanup_old_callbacks`
ON SCHEDULE EVERY 1 HOUR
DO
BEGIN
  DELETE FROM `wechat_pay_callback` 
  WHERE `created_at` < DATE_SUB(NOW(), INTERVAL 24 HOUR);
END//
DELIMITER ; 