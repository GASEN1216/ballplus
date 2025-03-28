-- 创建商品表
CREATE TABLE IF NOT EXISTS `product` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `name` varchar(255) NOT NULL COMMENT '商品名称',
  `price` int NOT NULL COMMENT '商品价格',
  `image` varchar(1024) NOT NULL COMMENT '商品图片URL',
  `type` varchar(50) NOT NULL COMMENT '商品类型',
  `description` varchar(500) DEFAULT NULL COMMENT '商品描述',
  `status` tinyint DEFAULT '1' COMMENT '商品状态 0-下架 1-上架',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商城商品表';

-- 插入初始数据
INSERT INTO `product` (`name`, `price`, `image`, `type`, `description`, `status`) VALUES
('dog1', 88, 'http://sunsetchat.top/user_avatars/gif/dog.gif', 'avatar', '可爱的小狗动态头像', 1),
('dog2', 88, 'http://sunsetchat.top/user_avatars/gif/dog2.gif', 'avatar', '另一款小狗动态头像', 1),
('加载中', 88, 'http://sunsetchat.top/user_avatars/gif/loading.gif', 'avatar', '加载中动态头像', 1),
('duck', 88, 'http://sunsetchat.top/user_avatars/gif/duck.gif', 'avatar', '可爱的小鸭子动态头像', 1); 