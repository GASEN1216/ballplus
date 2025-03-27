-- 创建投诉记录表
CREATE TABLE IF NOT EXISTS `complaint`
(
    `id`               bigint        NOT NULL AUTO_INCREMENT COMMENT '投诉ID',
    `event_id`         bigint        NOT NULL COMMENT '活动ID',
    `complainer_id`    int           NOT NULL COMMENT '投诉人ID',
    `complained_id`    int           NOT NULL COMMENT '被投诉人ID',
    `content`          varchar(500)  NOT NULL COMMENT '投诉内容',
    `status`           tinyint       NOT NULL DEFAULT 0 COMMENT '状态 0-待处理 1-有效 2-无效',
    `reject_reason`    varchar(255)           DEFAULT NULL COMMENT '拒绝原因',
    `create_time`      datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_event_id` (`event_id`),
    INDEX `idx_complainer_id` (`complainer_id`),
    INDEX `idx_complained_id` (`complained_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投诉记录表'; 