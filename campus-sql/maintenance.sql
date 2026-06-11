USE campus_service;

-- =========================================================
-- 维护脚本模板
-- 说明：
-- 1. 这份脚本用于后续维护，不用于全新建库。
-- 2. 执行前先备份数据库。
-- 3. 带 TODO 的值需要按实际情况修改后再执行。
-- =========================================================

-- =========================================================
-- 一、管理员维护
-- =========================================================

-- 1. 查看所有管理员
SELECT id, username, nickname, phone, email, role_id, status, create_time, update_time
FROM sys_user
WHERE role_id = 1
ORDER BY id;

-- 2. 将指定用户设置为管理员
-- TODO: 将 user_id 改为目标用户 ID
# UPDATE sys_user
# SET role_id = 1,
#     status = 1,
#     update_time = NOW()
# WHERE username = 'admin';

-- 3. 取消指定用户管理员权限，改为普通用户
-- TODO: 将 user_id 改为目标用户 ID，谨慎操作 admin
-- UPDATE sys_user
-- SET role_id = 2,
--     update_time = NOW()
-- WHERE id = user_id
--   AND username <> 'admin';

-- 4. 禁用指定用户
-- TODO: 将 user_id 改为目标用户 ID
-- UPDATE sys_user
-- SET status = 0,
--     update_time = NOW()
-- WHERE id = user_id;

-- 5. 启用指定用户
-- TODO: 将 user_id 改为目标用户 ID
-- UPDATE sys_user
-- SET status = 1,
--     update_time = NOW()
-- WHERE id = user_id;

-- 6. 重置用户名年度修改次数
-- TODO: 将 user_id 改为目标用户 ID
-- UPDATE sys_user
-- SET username_update_year = YEAR(CURDATE()),
--     username_update_count = 0,
--     update_time = NOW()
-- WHERE id = user_id;

-- =========================================================
-- 二、分类维护
-- =========================================================

-- 1. 查看分类
SELECT id, name, sort, status, create_time, update_time
FROM category
ORDER BY sort, id;

-- 2. 新增分类
-- TODO: 修改分类名和排序值
-- INSERT INTO category (name, sort, status, create_time, update_time)
-- VALUES ('新分类名称', 10, 1, NOW(), NOW());

-- 3. 停用分类
-- TODO: 将 category_id 改为目标分类 ID
-- UPDATE category
-- SET status = 0,
--     update_time = NOW()
-- WHERE id = category_id;

-- 4. 启用分类
-- TODO: 将 category_id 改为目标分类 ID
-- UPDATE category
-- SET status = 1,
--     update_time = NOW()
-- WHERE id = category_id;

-- =========================================================
-- 三、系统配置维护
-- =========================================================

-- 1. 查看系统配置
SELECT id, config_key, config_value, description, create_time, update_time
FROM sys_config
ORDER BY id;

-- 2. 开启自动审核
INSERT INTO sys_config (config_key, config_value, description, create_time, update_time)
VALUES ('AUTO_AUDIT_ENABLED', 'true', '内容自动审核开关', NOW(), NOW())
ON DUPLICATE KEY UPDATE
    config_value = 'true',
    description = VALUES(description),
    update_time = NOW();

-- 3. 关闭自动审核
-- UPDATE sys_config
-- SET config_value = 'false',
--     update_time = NOW()
-- WHERE config_key = 'AUTO_AUDIT_ENABLED';

-- =========================================================
-- 四、内容维护
-- =========================================================

-- 1. 查看待审核内容
SELECT id, title, category_id, user_id, status, is_private, create_time, update_time
FROM content
WHERE status = 0
ORDER BY create_time DESC;

-- 2. 将指定内容打回待审核
-- TODO: 将 content_id 改为目标内容 ID
-- UPDATE content
-- SET status = 0,
--     audit_reason = '管理员撤销发布，请修改后重新提交',
--     update_time = NOW()
-- WHERE id = content_id;

-- 3. 下架指定内容
-- TODO: 将 content_id 改为目标内容 ID
-- UPDATE content
-- SET status = 3,
--     audit_reason = '管理员下架',
--     update_time = NOW()
-- WHERE id = content_id;

-- 4. 置顶或取消置顶指定内容
-- TODO: 将 content_id 改为目标内容 ID，is_top_value 改为 1置顶 / 0取消置顶
-- UPDATE content
-- SET is_top = is_top_value,
--     update_time = NOW()
-- WHERE id = content_id;

-- 5. 给现有库补充内容置顶分页索引
-- ALTER TABLE content ADD KEY idx_content_category_status_top_time (category_id, status, is_private, is_top, create_time);

-- 6. 给现有库补充评论置顶字段和索引
-- 如已执行过或字段/索引已存在，重复执行会报重复错误，跳过即可。
ALTER TABLE `comment` ADD COLUMN is_top TINYINT DEFAULT 0 COMMENT '是否置顶：0否 1是' AFTER status;
ALTER TABLE `comment` ADD KEY idx_comment_content_status_parent_top_time (content_id, status, parent_id, is_top, create_time, id);

-- =========================================================
-- 五、好友分类升级维护
-- 说明：旧数据库升级好友分类功能时，只执行本区块未注释的 SQL。
-- 注意：如果你已经执行过本区块，ALTER TABLE 可能因字段/索引/外键已存在而报重复错误，重复执行时跳过对应 ALTER 即可。
-- =========================================================

-- 1. 现有库升级：创建好友分类表
CREATE TABLE IF NOT EXISTS `friend_category` (
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

-- 2. 现有库升级：给好友表添加分类字段
ALTER TABLE user_friend ADD COLUMN category_id BIGINT DEFAULT NULL COMMENT '好友分类ID' AFTER friend_id;
ALTER TABLE user_friend ADD KEY idx_friend_category_id (category_id);
ALTER TABLE user_friend ADD CONSTRAINT fk_user_friend_category
    FOREIGN KEY (category_id) REFERENCES friend_category (id)
    ON UPDATE CASCADE
    ON DELETE CASCADE;

-- 3. 为已有用户初始化默认好友分类
INSERT IGNORE INTO friend_category (user_id, name, sort, create_time, update_time)
SELECT id, '我的好友', 1, NOW(), NOW() FROM sys_user
UNION ALL
SELECT id, '家人', 2, NOW(), NOW() FROM sys_user
UNION ALL
SELECT id, '朋友', 3, NOW(), NOW() FROM sys_user
UNION ALL
SELECT id, '兄弟/姐妹', 4, NOW(), NOW() FROM sys_user
UNION ALL
SELECT id, '老师', 5, NOW(), NOW() FROM sys_user
UNION ALL
SELECT id, '陌生人', 6, NOW(), NOW() FROM sys_user;

-- 4. 将未分类好友放入“我的好友”
UPDATE user_friend uf
JOIN friend_category fc ON fc.user_id = uf.user_id AND fc.name = '我的好友'
SET uf.category_id = fc.id
WHERE uf.category_id IS NULL;

-- =========================================================
-- 六、数据检查
-- =========================================================

-- 1. 用户数量
SELECT COUNT(*) AS user_total FROM sys_user;

-- 2. 公开且已通过内容数量
SELECT COUNT(*) AS public_passed_content_total
FROM content
WHERE status = 1
  AND IFNULL(is_private, 0) = 0;

-- 3. 分类下内容数量
SELECT c.id, c.name, c.sort, COUNT(ct.id) AS content_total
FROM category c
LEFT JOIN content ct ON ct.category_id = c.id
GROUP BY c.id, c.name, c.sort
ORDER BY c.sort, c.id;

-- =========================================================
-- 七、活动通知子分类升级维护
-- 说明：现有数据库升级“活动通知下二级分类”时，执行本区块未注释的 SQL。
-- 注意：如果字段、索引或外键已存在，重复执行对应 ALTER 会报重复错误，跳过即可。
-- =========================================================

-- 1. 创建活动通知子分类表
CREATE TABLE IF NOT EXISTS `activity_sub_category` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    name VARCHAR(50) NOT NULL COMMENT '活动通知子分类名称',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：1启用 0停用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_activity_sub_category_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动通知子分类表';

-- 2. 给内容表添加活动通知子分类字段和索引
ALTER TABLE content ADD COLUMN activity_sub_category_id BIGINT DEFAULT NULL COMMENT '活动通知子分类ID' AFTER category_id;
ALTER TABLE content ADD KEY idx_activity_sub_category_id (activity_sub_category_id);
ALTER TABLE content ADD KEY idx_content_activity_sub_status_top_time (activity_sub_category_id, status, is_private, is_top, create_time);
ALTER TABLE content ADD CONSTRAINT fk_content_activity_sub_category
    FOREIGN KEY (activity_sub_category_id) REFERENCES activity_sub_category (id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT;

-- 3. 初始化常用活动通知子分类
INSERT IGNORE INTO activity_sub_category (name, sort, status, create_time, update_time) VALUES
    ('理学院', 1, 1, NOW(), NOW()),
    ('大数据工程学院', 2, 1, NOW(), NOW()),
    ('社团活动', 3, 1, NOW(), NOW()),
    ('教务通知', 4, 1, NOW(), NOW());

-- =========================================================
-- 八、用户主页背景字段升级
-- 现有数据库执行本段，用于保存用户主页背景并支持旧资源清理。
-- =========================================================

SET @has_profile_background := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_user'
      AND COLUMN_NAME = 'profile_background'
);
SET @profile_background_sql := IF(
    @has_profile_background = 0,
    'ALTER TABLE sys_user ADD COLUMN profile_background VARCHAR(255) DEFAULT NULL COMMENT ''用户主页背景'' AFTER avatar',
    'SELECT ''profile_background already exists'''
);
PREPARE profile_background_stmt FROM @profile_background_sql;
EXECUTE profile_background_stmt;
DEALLOCATE PREPARE profile_background_stmt;
