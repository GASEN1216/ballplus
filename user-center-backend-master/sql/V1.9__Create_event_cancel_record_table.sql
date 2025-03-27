-- 创建活动取消记录表
CREATE TABLE IF NOT EXISTS `event_cancel_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `event_id` bigint(20) NOT NULL COMMENT '活动ID',
  `cancel_reason` varchar(255) NOT NULL COMMENT '取消原因',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_event_id` (`event_id`) COMMENT '活动ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动取消记录表'; 