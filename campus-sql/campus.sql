DROP DATABASE IF EXISTS campus_service;
CREATE DATABASE campus_service
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE campus_service;

CREATE TABLE `role` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    status TINYINT DEFAULT 1 COMMENT '状态：1启用 0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

CREATE TABLE `sys_user` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    username VARCHAR(20) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    nickname VARCHAR(16) NOT NULL COMMENT '昵称',
    signature VARCHAR(200) DEFAULT NULL COMMENT '个性签名',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像',
    profile_background VARCHAR(255) DEFAULT NULL COMMENT '用户主页背景',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    phone_visible TINYINT DEFAULT 0 COMMENT '手机号公开状态：0隐藏 1公开',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    email_visible TINYINT DEFAULT 0 COMMENT '邮箱公开状态：0隐藏 1公开',
    wechat VARCHAR(50) DEFAULT NULL COMMENT '微信号',
    wechat_visible TINYINT DEFAULT 0 COMMENT '微信公开状态：0隐藏 1公开',
    qq VARCHAR(20) DEFAULT NULL COMMENT 'QQ号',
    qq_visible TINYINT DEFAULT 0 COMMENT 'QQ公开状态：0隐藏 1公开',
    private_chat_enabled TINYINT DEFAULT 1 COMMENT '是否允许私聊：0关闭 1开启',
    gender TINYINT DEFAULT 1 COMMENT '性别：0保密 1男 2女',
    role_id BIGINT DEFAULT 2 COMMENT '角色ID',
    status TINYINT DEFAULT 1 COMMENT '状态：1启用 0禁用',
    username_update_year INT DEFAULT NULL COMMENT '用户名最近修改年份',
    username_update_count INT NOT NULL DEFAULT 0 COMMENT '用户名当年修改次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_phone (phone),
    UNIQUE KEY uk_email (email),
    KEY idx_role_id (role_id),
    CONSTRAINT fk_user_role
        FOREIGN KEY (role_id) REFERENCES `role` (id)
            ON UPDATE CASCADE
            ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE `category` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：1启用 0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_category_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

CREATE TABLE `activity_sub_category` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    name VARCHAR(50) NOT NULL COMMENT '活动通知子分类名称',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：1启用 0停用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_activity_sub_category_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动通知子分类表';

CREATE TABLE `content` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    title VARCHAR(100) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    cover_img VARCHAR(255) DEFAULT NULL COMMENT '封面图',
    media_urls TEXT DEFAULT NULL COMMENT '帖子图片和视频URL列表，JSON数组',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    activity_sub_category_id BIGINT DEFAULT NULL COMMENT '活动通知子分类ID',
    user_id BIGINT NOT NULL COMMENT '发布者ID',
    status TINYINT DEFAULT 0 COMMENT '状态：0待审核 1已通过 2已拒绝 3已下架',
    is_private TINYINT NOT NULL DEFAULT 0 COMMENT '是否私密：0公开 1私密',
    audit_reason VARCHAR(255) DEFAULT NULL COMMENT '审核原因',
    is_top TINYINT DEFAULT 0 COMMENT '是否置顶：0否 1是',
    view_count INT DEFAULT 0 COMMENT '浏览量',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    favorite_count INT DEFAULT 0 COMMENT '收藏数',
    comment_count INT DEFAULT 0 COMMENT '评论数',
    price DECIMAL(10,2) DEFAULT NULL COMMENT '二手交易价格',
    trade_type VARCHAR(50) DEFAULT NULL COMMENT '交易物品分类',
    trade_condition VARCHAR(30) DEFAULT NULL COMMENT '新旧程度',
    trade_status TINYINT DEFAULT 0 COMMENT '交易状态：0在售 1已预定 2已售出',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_category_id (category_id),
    KEY idx_activity_sub_category_id (activity_sub_category_id),
    KEY idx_user_id (user_id),
    KEY idx_status (status),
    KEY idx_is_private (is_private),
    KEY idx_create_time (create_time),
    KEY idx_title (title),
    KEY idx_content_category_status_top_time (category_id, status, is_private, is_top, create_time),
    KEY idx_content_activity_sub_status_top_time (activity_sub_category_id, status, is_private, is_top, create_time),
    KEY idx_content_public_hot (status, is_private, view_count, like_count, favorite_count),
    CONSTRAINT fk_content_category
        FOREIGN KEY (category_id) REFERENCES `category` (id)
            ON UPDATE CASCADE
            ON DELETE RESTRICT,
    CONSTRAINT fk_content_activity_sub_category
        FOREIGN KEY (activity_sub_category_id) REFERENCES `activity_sub_category` (id)
            ON UPDATE CASCADE
            ON DELETE RESTRICT,
    CONSTRAINT fk_content_user
        FOREIGN KEY (user_id) REFERENCES `sys_user` (id)
            ON UPDATE CASCADE
            ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容表';

CREATE TABLE `audit_record` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    content_id BIGINT NOT NULL COMMENT '内容ID',
    auditor_id BIGINT NOT NULL COMMENT '审核人ID',
    audit_result TINYINT NOT NULL COMMENT '审核结果：1通过 2拒绝',
    audit_reason VARCHAR(255) DEFAULT NULL COMMENT '审核原因',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_content_id (content_id),
    KEY idx_auditor_id (auditor_id),
    KEY idx_create_time (create_time),
    CONSTRAINT fk_audit_content
        FOREIGN KEY (content_id) REFERENCES `content` (id)
            ON UPDATE CASCADE
            ON DELETE CASCADE,
    CONSTRAINT fk_audit_auditor
        FOREIGN KEY (auditor_id) REFERENCES `sys_user` (id)
            ON UPDATE CASCADE
            ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核记录表';

CREATE TABLE `sys_config` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    config_key VARCHAR(100) NOT NULL COMMENT '配置KEY',
    config_value VARCHAR(255) NOT NULL COMMENT '配置值',
    description VARCHAR(255) DEFAULT NULL COMMENT '配置说明',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

CREATE TABLE `favorite` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    content_id BIGINT NOT NULL COMMENT '内容ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    UNIQUE KEY uk_user_content (user_id, content_id),
    KEY idx_user_id (user_id),
    KEY idx_content_id (content_id),
    CONSTRAINT fk_favorite_user
        FOREIGN KEY (user_id) REFERENCES `sys_user` (id)
            ON UPDATE CASCADE
            ON DELETE CASCADE,
    CONSTRAINT fk_favorite_content
        FOREIGN KEY (content_id) REFERENCES `content` (id)
            ON UPDATE CASCADE
            ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

CREATE TABLE `like_record` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    content_id BIGINT NOT NULL COMMENT '内容ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    UNIQUE KEY uk_user_content_like (user_id, content_id),
    KEY idx_user_id (user_id),
    KEY idx_content_id (content_id),
    KEY idx_like_record_user_content (user_id, content_id),
    KEY idx_like_record_content (content_id),
    CONSTRAINT fk_like_user
        FOREIGN KEY (user_id) REFERENCES `sys_user` (id)
            ON UPDATE CASCADE
            ON DELETE CASCADE,
    CONSTRAINT fk_like_content
        FOREIGN KEY (content_id) REFERENCES `content` (id)
            ON UPDATE CASCADE
            ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞表';

CREATE TABLE `comment` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    content_id BIGINT NOT NULL COMMENT '内容ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    parent_id BIGINT DEFAULT NULL COMMENT '父评论ID',
    comment_text VARCHAR(500) NOT NULL COMMENT '评论内容',
    status TINYINT DEFAULT 1 COMMENT '状态：1正常 0删除',
    is_top TINYINT DEFAULT 0 COMMENT '是否置顶：0否 1是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_content_id (content_id),
    KEY idx_user_id (user_id),
    KEY idx_parent_id (parent_id),
    KEY idx_create_time (create_time),
    KEY idx_comment_content_status_parent_top_time (content_id, status, parent_id, is_top, create_time, id),
    CONSTRAINT fk_comment_content
        FOREIGN KEY (content_id) REFERENCES `content` (id)
            ON UPDATE CASCADE
            ON DELETE CASCADE,
    CONSTRAINT fk_comment_user
        FOREIGN KEY (user_id) REFERENCES `sys_user` (id)
            ON UPDATE CASCADE
            ON DELETE CASCADE,
    CONSTRAINT fk_comment_parent
        FOREIGN KEY (parent_id) REFERENCES `comment` (id)
            ON UPDATE CASCADE
            ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

CREATE TABLE `comment_reaction` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    comment_id BIGINT NOT NULL COMMENT '评论ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    reaction_type TINYINT NOT NULL COMMENT '反应类型：1赞 2踩',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_comment (user_id, comment_id),
    KEY idx_comment_id (comment_id),
    KEY idx_user_id (user_id),
    KEY idx_comment_reaction_comment_user_type (comment_id, user_id, reaction_type),
    KEY idx_comment_reaction_user_comment (user_id, comment_id),
    CONSTRAINT fk_comment_reaction_comment
        FOREIGN KEY (comment_id) REFERENCES `comment` (id)
            ON UPDATE CASCADE
            ON DELETE CASCADE,
    CONSTRAINT fk_comment_reaction_user
        FOREIGN KEY (user_id) REFERENCES `sys_user` (id)
            ON UPDATE CASCADE
            ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论反应表';

CREATE TABLE `friend_category` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    name VARCHAR(20) NOT NULL COMMENT '分类名称',
    sort INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_friend_category_user_name (user_id, name),
    KEY idx_friend_category_user (user_id),
    CONSTRAINT fk_friend_category_user
        FOREIGN KEY (user_id) REFERENCES `sys_user` (id)
            ON UPDATE CASCADE
            ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友分类表';

CREATE TABLE `user_friend` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    friend_id BIGINT NOT NULL COMMENT '好友用户ID',
    category_id BIGINT DEFAULT NULL COMMENT '好友分类ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
    UNIQUE KEY uk_user_friend (user_id, friend_id),
    KEY idx_user_id (user_id),
    KEY idx_friend_id (friend_id),
    KEY idx_friend_category_id (category_id),
    CONSTRAINT fk_user_friend_user
        FOREIGN KEY (user_id) REFERENCES `sys_user` (id)
            ON UPDATE CASCADE
            ON DELETE CASCADE,
    CONSTRAINT fk_user_friend_friend
        FOREIGN KEY (friend_id) REFERENCES `sys_user` (id)
            ON UPDATE CASCADE
            ON DELETE CASCADE,
    CONSTRAINT fk_user_friend_category
        FOREIGN KEY (category_id) REFERENCES `friend_category` (id)
            ON UPDATE CASCADE
            ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户好友表';

CREATE TABLE `notice` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    title VARCHAR(100) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    hero_background VARCHAR(500) DEFAULT NULL COMMENT '首页大卡片背景URL',
    is_top TINYINT DEFAULT 0 COMMENT '是否置顶：0否 1是',
    status TINYINT DEFAULT 1 COMMENT '状态：1发布 0草稿',
    publisher_id BIGINT NOT NULL COMMENT '发布人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_publisher_id (publisher_id),
    KEY idx_is_top (is_top),
    KEY idx_create_time (create_time),
    CONSTRAINT fk_notice_publisher
        FOREIGN KEY (publisher_id) REFERENCES `sys_user` (id)
            ON UPDATE CASCADE
            ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

CREATE TABLE `report` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '举报人ID',
    content_id BIGINT NOT NULL COMMENT '被举报内容ID',
    report_type VARCHAR(50) NOT NULL COMMENT '举报类型',
    report_reason VARCHAR(255) NOT NULL COMMENT '举报原因',
    status TINYINT DEFAULT 0 COMMENT '状态：0待处理 1已处理',
    handler_id BIGINT DEFAULT NULL COMMENT '处理人ID',
    handle_result VARCHAR(255) DEFAULT NULL COMMENT '处理结果',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    handle_time DATETIME DEFAULT NULL COMMENT '处理时间',
    KEY idx_user_id (user_id),
    KEY idx_content_id (content_id),
    KEY idx_status (status),
    KEY idx_create_time (create_time),
    KEY idx_handler_id (handler_id),
    CONSTRAINT fk_report_user
        FOREIGN KEY (user_id) REFERENCES `sys_user` (id)
            ON UPDATE CASCADE
            ON DELETE CASCADE,
    CONSTRAINT fk_report_content
        FOREIGN KEY (content_id) REFERENCES `content` (id)
            ON UPDATE CASCADE
            ON DELETE CASCADE,
    CONSTRAINT fk_report_handler
        FOREIGN KEY (handler_id) REFERENCES `sys_user` (id)
            ON UPDATE CASCADE
            ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报表';

CREATE TABLE `message` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '接收用户ID',
    title VARCHAR(100) NOT NULL COMMENT '消息标题',
    content VARCHAR(500) NOT NULL COMMENT '消息内容',
    type VARCHAR(50) NOT NULL COMMENT '消息类型',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读：0未读 1已读',
    related_id BIGINT DEFAULT NULL COMMENT '关联业务ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_user_id (user_id),
    KEY idx_is_read (is_read),
    KEY idx_type (type),
    KEY idx_create_time (create_time),
    CONSTRAINT fk_message_user
        FOREIGN KEY (user_id) REFERENCES `sys_user` (id)
            ON UPDATE CASCADE
            ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

CREATE TABLE `user_block` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    blocker_id BIGINT NOT NULL COMMENT '屏蔽者ID',
    blocked_user_id BIGINT NOT NULL COMMENT '被屏蔽者ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_blocker_blocked (blocker_id, blocked_user_id),
    KEY idx_blocker_id (blocker_id),
    KEY idx_blocked_user_id (blocked_user_id),
    CONSTRAINT fk_user_blocker
        FOREIGN KEY (blocker_id) REFERENCES `sys_user` (id)
            ON UPDATE CASCADE
            ON DELETE CASCADE,
    CONSTRAINT fk_user_blocked
        FOREIGN KEY (blocked_user_id) REFERENCES `sys_user` (id)
            ON UPDATE CASCADE
            ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户屏蔽表';

CREATE TABLE `private_message` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    sender_id BIGINT NOT NULL COMMENT '发送者ID',
    receiver_id BIGINT NOT NULL COMMENT '接收者ID',
    content VARCHAR(1000) NOT NULL COMMENT '消息内容',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_sender_id (sender_id),
    KEY idx_receiver_id (receiver_id),
    KEY idx_create_time (create_time),
    KEY idx_private_msg_pair_after_id (sender_id, receiver_id, id),
    KEY idx_private_msg_receiver_read_sender (receiver_id, is_read, sender_id),
    KEY idx_private_msg_user_time (sender_id, receiver_id, create_time, id),
    CONSTRAINT fk_private_message_sender
        FOREIGN KEY (sender_id) REFERENCES `sys_user` (id)
            ON UPDATE CASCADE
            ON DELETE CASCADE,
    CONSTRAINT fk_private_message_receiver
        FOREIGN KEY (receiver_id) REFERENCES `sys_user` (id)
            ON UPDATE CASCADE
            ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='私聊消息表';

INSERT INTO `role` (role_name, role_code, status) VALUES
    ('管理员', 'ADMIN', 1),
    ('普通用户', 'USER', 1);

INSERT INTO `category` (name, sort, status) VALUES
    ('失物招领', 1, 1),
    ('二手交易', 2, 1),
    ('活动通知', 3, 1),
    ('校园论坛', 6, 1);

INSERT INTO `activity_sub_category` (name, sort, status) VALUES
    ('理学院', 1, 1),
    ('大数据工程学院', 2, 1),
    ('社团活动', 3, 1),
    ('教务通知', 4, 1);

INSERT INTO `sys_config` (config_key, config_value, description) VALUES
    ('AUTO_AUDIT_ENABLED', 'true', '内容自动审核开关');
