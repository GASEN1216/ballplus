-- 插入资源数据
INSERT INTO `resource` (`title`, `description`, `cover_image`, `type`, `link`, `views`, `likes`, `category`) VALUES
    ('Spring Boot 3.0 入门教程', '本教程将带你快速入门Spring Boot 3.0的核心特性和基本用法', 'https://example.com/images/spring-boot.jpg', 'article', 'https://example.com/articles/spring-boot-tutorial', 1200, 350, 'Java'),
    ('Vue.js 3.0 实战指南', '从零开始学习Vue.js 3.0，包含组合式API、响应式系统等核心概念', 'https://example.com/images/vue3.jpg', 'video', 'https://example.com/videos/vue3-guide', 2500, 820, 'Frontend'),
    ('微信小程序开发实战', '完整的微信小程序开发教程，从基础到高级功能的详细讲解', 'https://example.com/images/miniprogram.jpg', 'video', 'https://example.com/videos/miniprogram-dev', 3600, 960, 'Frontend'),
    ('MySQL性能优化指南', '深入解析MySQL索引优化、查询优化、配置优化等关键技术', 'https://example.com/images/mysql.jpg', 'article', 'https://example.com/articles/mysql-optimization', 1800, 560, 'Database'),
    ('Docker容器化部署教程', '详细讲解Docker的基本概念、常用命令和实际部署案例', 'https://example.com/images/docker.jpg', 'video', 'https://example.com/videos/docker-deploy', 2200, 680, 'DevOps'),
    ('React Native移动开发', 'React Native跨平台移动应用开发的完整教程', 'https://example.com/images/react-native.jpg', 'article', 'https://example.com/articles/react-native', 1500, 420, 'Mobile'),
    ('Python数据分析实战', '使用Python进行数据分析的实用技巧和项目案例', 'https://example.com/images/python-data.jpg', 'video', 'https://example.com/videos/python-analysis', 2800, 750, 'Python'),
    ('Git版本控制精讲', 'Git的核心概念、工作流程和团队协作最佳实践', 'https://example.com/images/git.jpg', 'article', 'https://example.com/articles/git-guide', 1900, 580, 'Tools'),
    ('Flutter UI设计指南', 'Flutter跨平台UI设计和开发的详细教程', 'https://example.com/images/flutter.jpg', 'video', 'https://example.com/videos/flutter-ui', 2100, 640, 'Mobile'),
    ('Redis缓存设计方案', 'Redis缓存在实际项目中的应用和优化策略', 'https://example.com/images/redis.jpg', 'article', 'https://example.com/articles/redis-cache', 1600, 480, 'Database');

-- 插入用户收藏数据（假设用户ID为1、2、3）
INSERT INTO `user_favorite` (`user_id`, `resource_id`) VALUES
    (1, 1),  -- 用户1收藏了Spring Boot教程
    (1, 3),  -- 用户1收藏了微信小程序教程
    (1, 5),  -- 用户1收藏了Docker教程
    (2, 2),  -- 用户2收藏了Vue.js教程
    (2, 4),  -- 用户2收藏了MySQL教程
    (2, 7),  -- 用户2收藏了Python教程
    (3, 3),  -- 用户3收藏了微信小程序教程
    (3, 6),  -- 用户3收藏了React Native教程
    (3, 9),  -- 用户3收藏了Flutter教程
    (1, 8),  -- 用户1收藏了Git教程
    (2, 10), -- 用户2收藏了Redis教程
    (3, 1);  -- 用户3收藏了Spring Boot教程 