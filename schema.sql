CREATE DATABASE IF NOT EXISTS bomb
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_unicode_ci;

USE bomb;

-- =========================
-- 用户表
-- =========================
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(100) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `email` VARCHAR(255) DEFAULT NULL COMMENT '邮箱',
    `role` VARCHAR(50) NOT NULL DEFAULT 'USER' COMMENT '角色：USER/ADMIN',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_user_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- =========================
-- 知识分类表
-- =========================
CREATE TABLE IF NOT EXISTS `category` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    `name` VARCHAR(100) NOT NULL COMMENT '分类名称',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '分类说明',
    `sort_order` INT DEFAULT 0 COMMENT '排序值',
    `status` INT DEFAULT 1 COMMENT '状态：1启用，0停用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识分类表';

-- =========================
-- 知识库表
-- =========================
CREATE TABLE IF NOT EXISTS `knowledge` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '知识ID',
    `category_id` BIGINT DEFAULT NULL COMMENT '分类ID',
    `question` VARCHAR(500) NOT NULL COMMENT '问题',
    `answer` LONGTEXT NOT NULL COMMENT '答案',
    `keywords` VARCHAR(500) DEFAULT NULL COMMENT '关键词',
    `view_count` INT DEFAULT 0 COMMENT '浏览量/使用次数',
    `source_type` VARCHAR(100) DEFAULT NULL COMMENT '来源类型',
    `status` INT DEFAULT 1 COMMENT '状态：1启用，0停用',
    `contributor` VARCHAR(100) DEFAULT NULL COMMENT '贡献者',
    `admin_note` VARCHAR(500) DEFAULT NULL COMMENT '管理员备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY `idx_knowledge_category_id` (`category_id`),
    KEY `idx_knowledge_status` (`status`),
    KEY `idx_knowledge_update_time` (`update_time`),
    KEY `idx_knowledge_question` (`question`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库表';

-- =========================
-- 聊天会话表
-- =========================
CREATE TABLE IF NOT EXISTS `chat_conversation` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '会话ID',
    `username` VARCHAR(100) NOT NULL COMMENT '所属用户名',
    `title` VARCHAR(255) NOT NULL COMMENT '会话标题',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY `idx_chat_conversation_username` (`username`),
    KEY `idx_chat_conversation_update_time` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天会话表';

-- =========================
-- 聊天消息表
-- =========================
CREATE TABLE IF NOT EXISTS `chat_message` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    `conversation_id` BIGINT NOT NULL COMMENT '会话ID',
    `role` VARCHAR(50) NOT NULL COMMENT '角色：user/assistant',
    `content` LONGTEXT NOT NULL COMMENT '消息内容',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY `idx_chat_message_conversation_id` (`conversation_id`),
    KEY `idx_chat_message_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

-- =========================
-- 用户反馈表
-- =========================
CREATE TABLE IF NOT EXISTS `feedback` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '反馈ID',
    `username` VARCHAR(100) DEFAULT NULL COMMENT '提交用户',
    `content` LONGTEXT NOT NULL COMMENT '反馈内容',
    `status` VARCHAR(50) DEFAULT 'PENDING' COMMENT '状态：PENDING/REPLIED',
    `reply` LONGTEXT DEFAULT NULL COMMENT '管理员回复',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    `reply_time` DATETIME DEFAULT NULL COMMENT '回复时间',
    KEY `idx_feedback_username` (`username`),
    KEY `idx_feedback_status` (`status`),
    KEY `idx_feedback_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户反馈表';

-- =========================
-- 初始化分类数据
-- =========================
INSERT INTO `category` (`id`, `name`, `description`, `sort_order`, `status`)
VALUES
(1, '食堂餐饮', '食堂、饭堂、餐饮、吃饭相关问题', 1, 1),
(2, '校园卡服务', '校园卡挂失、补办、充值等问题', 2, 1),
(3, '宿舍服务', '宿舍报修、水电、空调等问题', 3, 1),
(4, '图书馆服务', '借书、还书、续借、自习等问题', 4, 1),
(5, '教务通知', '课程、考试、成绩、选课等问题', 5, 1),
(6, '校园生活', '其他校园生活类问题', 6, 1)
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`description` = VALUES(`description`),
`sort_order` = VALUES(`sort_order`),
`status` = VALUES(`status`);

-- =========================
-- 初始化管理员账号示例
-- 密码请在正式使用时修改
-- =========================
INSERT INTO `user` (`username`, `password`, `email`, `role`)
VALUES
('admin', '123456', NULL, 'ADMIN')
ON DUPLICATE KEY UPDATE
`role` = 'ADMIN';