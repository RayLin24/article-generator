-- 创建数据库
CREATE DATABASE IF NOT EXISTS article_generator DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE article_generator;

-- 栏目表
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name` VARCHAR(50) NOT NULL COMMENT '栏目名称',
    `code` VARCHAR(50) NOT NULL COMMENT '栏目编码',
    `prompt_template` TEXT COMMENT 'AI生成提示词模板',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态(0禁用/1启用)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='栏目表';

-- 文章表
DROP TABLE IF EXISTS `article`;
CREATE TABLE `article` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `category_id` BIGINT NOT NULL COMMENT '栏目ID',
    `title` VARCHAR(200) NOT NULL COMMENT '文章标题',
    `content` LONGTEXT COMMENT '文章内容',
    `summary` VARCHAR(500) COMMENT '摘要',
    `cover_image` VARCHAR(255) COMMENT '封面图URL',
    `publish_date` DATE COMMENT '发布日期',
    `view_count` INT DEFAULT 0 COMMENT '阅读量',
    `status` TINYINT DEFAULT 1 COMMENT '状态(0草稿/1已发布)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_publish_date` (`publish_date`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章表';

-- 插入初始栏目数据
INSERT INTO `category` (`name`, `code`, `prompt_template`, `sort_order`, `status`) VALUES
('历史故事', 'history', '请写一篇关于{topic}的历史故事。要求：1. 文笔优美，叙事生动；2. 内容真实，有历史依据；3. 字数约1500-2000字；4. 包含历史背景、人物描写、事件经过和深远影响。', 1, 1),
('爱情故事', 'love', '请写一篇关于{topic}的爱情故事。要求：1. 情感真挚，动人心弦；2. 人物形象鲜明；3. 字数约1500-2000字；4. 有起承转合，结局温暖或发人深省。', 2, 1),
('经典寓言', 'fable', '请写一篇关于{topic}的寓言故事。要求：1. 故事简洁有力；2. 寓意深刻，富有哲理；3. 字数约800-1200字；4. 适合各年龄段读者，结尾点明寓意。', 3, 1),
('热点新闻', 'news', '请根据今日热点新闻，写一篇关于{topic}的新闻评论文章。要求：1. 观点鲜明，有深度分析；2. 引用相关背景资料；3. 字数约1000-1500字；4. 客观公正，有独到见解。', 4, 1),
('科技热点', 'tech', '请写一篇关于{topic}的科技热点文章。要求：1. 通俗易懂，深入浅出；2. 包含技术原理和应用场景；3. 字数约1500-2000字；4. 展望未来发展趋势。', 5, 1);
