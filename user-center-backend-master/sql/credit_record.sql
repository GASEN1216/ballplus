CREATE TABLE `credit_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `credit_change` int(11) NOT NULL COMMENT '信誉分变动值（正数为增加，负数为减少）',
  `change_type` tinyint(4) NOT NULL COMMENT '变动原因类型：1-投诉扣分，2-取消活动扣分，3-退出活动扣分，4-管理员调整，5-其他原因',
  `change_reason` varchar(255) DEFAULT NULL COMMENT '详细原因',
  `relation_id` bigint(20) DEFAULT NULL COMMENT '关联记录ID（如投诉ID、活动ID等）',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引',
  KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信誉分记录表'; 