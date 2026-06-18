USE campus_service;
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `activity_sub_category` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    name VARCHAR(50) NOT NULL COMMENT '活动通知子分类名称',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：1启用 0停用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_activity_sub_category_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动通知子分类表';

SET @sql = IF(
    (SELECT COUNT(1)
     FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE()
       AND TABLE_NAME = 'content'
       AND COLUMN_NAME = 'activity_sub_category_id') = 0,
    'ALTER TABLE content ADD COLUMN activity_sub_category_id BIGINT DEFAULT NULL COMMENT ''活动通知子分类ID'' AFTER category_id',
    'SELECT ''content.activity_sub_category_id already exists'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(1)
     FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = DATABASE()
       AND TABLE_NAME = 'content'
       AND INDEX_NAME = 'idx_activity_sub_category_id') = 0,
    'ALTER TABLE content ADD KEY idx_activity_sub_category_id (activity_sub_category_id)',
    'SELECT ''idx_activity_sub_category_id already exists'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(1)
     FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = DATABASE()
       AND TABLE_NAME = 'content'
       AND INDEX_NAME = 'idx_content_activity_sub_status_top_time') = 0,
    'ALTER TABLE content ADD KEY idx_content_activity_sub_status_top_time (activity_sub_category_id, status, is_private, is_top, create_time)',
    'SELECT ''idx_content_activity_sub_status_top_time already exists'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(1)
     FROM information_schema.TABLE_CONSTRAINTS
     WHERE CONSTRAINT_SCHEMA = DATABASE()
       AND TABLE_NAME = 'content'
       AND CONSTRAINT_NAME = 'fk_content_activity_sub_category') = 0,
    'ALTER TABLE content ADD CONSTRAINT fk_content_activity_sub_category FOREIGN KEY (activity_sub_category_id) REFERENCES activity_sub_category (id) ON UPDATE CASCADE ON DELETE RESTRICT',
    'SELECT ''fk_content_activity_sub_category already exists'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

INSERT IGNORE INTO activity_sub_category (name, sort, status, create_time, update_time) VALUES
    ('理学院', 1, 1, NOW(), NOW()),
    ('大数据工程学院', 2, 1, NOW(), NOW()),
    ('社团活动', 3, 1, NOW(), NOW()),
    ('教务通知', 4, 1, NOW(), NOW());




ALTER TABLE `comment`
    ADD COLUMN is_top TINYINT DEFAULT 0 COMMENT '是否置顶：0否 1是' AFTER status;

ALTER TABLE `comment`
    ADD KEY idx_comment_content_status_parent_top_time
        (content_id, status, parent_id, is_top, create_time, id);