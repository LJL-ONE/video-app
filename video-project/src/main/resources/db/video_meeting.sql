-- ============================================================
-- 视频社区后台管理系统 - 数据库初始化脚本
-- 适用 MySQL 8.0+
-- 数据库：video_meeting
-- ============================================================

CREATE DATABASE IF NOT EXISTS `video_meeting`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `video_meeting`;

-- ------------------------------------------------------------
-- 用户表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`    VARCHAR(50)  NOT NULL COMMENT '登录账号',
    `password`    VARCHAR(100) NOT NULL COMMENT '密码（生产请存 BCrypt）',
    `nickname`    VARCHAR(50)           DEFAULT NULL COMMENT '昵称',
    `role`        VARCHAR(20)  NOT NULL DEFAULT '普通用户' COMMENT '角色：管理员/普通用户',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删 1-已删',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 初始账号（密码 123456 的 BCrypt 加密值）
-- BCrypt 加密后的密码：$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi
INSERT INTO `user` (`username`, `password`, `nickname`, `role`) VALUES
    ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '超级管理员', '管理员'),
    ('test',  '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '测试普通用户', '普通用户'),
    ('user01','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '视频创作者',  '普通用户');

-- ------------------------------------------------------------
-- 视频表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `video`;
CREATE TABLE `video` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `title`       VARCHAR(200) NOT NULL COMMENT '视频标题',
    `category`    VARCHAR(50)  NOT NULL COMMENT '分类',
    `file_key`    VARCHAR(255)          DEFAULT NULL COMMENT 'MinIO 中的对象 key',
    `file_url`    VARCHAR(500)          DEFAULT NULL COMMENT '视频访问 URL',
    `cover_url`   VARCHAR(500)          DEFAULT NULL COMMENT '封面图 URL',
    `size`        BIGINT                DEFAULT NULL COMMENT '文件大小（字节）',
    `duration`    INT                   DEFAULT NULL COMMENT '时长（秒）',
    `status`      VARCHAR(20)  NOT NULL DEFAULT '待审核' COMMENT '状态：待审核/已发布/已下架',
    `uploader`    VARCHAR(50)           DEFAULT NULL COMMENT '上传者账号',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category`),
    KEY `idx_status`   (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频表';

INSERT INTO `video` (`title`, `category`, `status`, `uploader`) VALUES
    ('Vue3+Vite前端搭建教程',  '技术分享', '已发布', 'admin'),
    ('日常vlog记录',          '生活日常', '已发布', 'test'),
    ('需求评审会议录屏',      '会议录屏', '待审核', 'admin');

-- ------------------------------------------------------------
-- 会议房间表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `meeting_room`;
CREATE TABLE `meeting_room` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `room_name`   VARCHAR(100) NOT NULL COMMENT '房间名称',
    `description` VARCHAR(500)          DEFAULT NULL COMMENT '房间描述',
    `creator`     VARCHAR(50)  NOT NULL COMMENT '创建者账号',
    `status`      VARCHAR(20)  NOT NULL DEFAULT '空闲' COMMENT '状态：空闲/使用中',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会议房间表';

INSERT INTO `meeting_room` (`room_name`, `description`, `creator`, `status`) VALUES
    ('技术需求评审会议室', '需求评审专用',  'admin', '使用中'),
    ('前端联调会议室',     '前后端联调用',  'test',  '空闲'),
    ('产品方案讨论室',     '产品方案讨论',  'admin', '使用中');
