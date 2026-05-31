-- =========================
-- 1. 创建数据库
-- =========================
create database if not exists campus_service
    default character set utf8mb4
    collate utf8mb4_general_ci;

use campus_service;

-- =========================
-- 2. 先删除表（按依赖反向删除）
-- =========================
drop table if exists `message`;
drop table if exists `report`;
drop table if exists `comment`;
drop table if exists `like_record`;
drop table if exists `favorite`;
drop table if exists `audit_record`;
drop table if exists `content`;
drop table if exists `category`;
drop table if exists `sys_user`;
drop table if exists `role`;

-- =========================
-- 3. 角色表
-- =========================
create table `role` (
                        id bigint primary key auto_increment comment '主键',
                        role_name varchar(50) not null comment '角色名称',
                        role_code varchar(50) not null comment '角色编码',
                        status tinyint default 1 comment '状态：1启用 0禁用',
                        create_time datetime default current_timestamp comment '创建时间',
                        update_time datetime default current_timestamp on update current_timestamp comment '更新时间',
                        unique key uk_role_code (role_code)
) engine=InnoDB default charset=utf8mb4 comment='角色表';

-- =========================
-- 4. 用户表
-- =========================
create table `sys_user` (
                            id bigint primary key auto_increment comment '主键',
                            username varchar(50) not null comment '用户名',
                            password varchar(100) not null comment '密码',
                            nickname varchar(50) default null comment '昵称',
                            avatar varchar(255) default null comment '头像',
                            phone varchar(20) default null comment '手机号',
                            email varchar(100) default null comment '邮箱',
                            gender tinyint default 0 comment '性别：0未知 1男 2女',
                            role_id bigint default 2 comment '角色ID',
                            status tinyint default 1 comment '状态：1启用 0禁用',
                            create_time datetime default current_timestamp comment '创建时间',
                            update_time datetime default current_timestamp on update current_timestamp comment '更新时间',
                            unique key uk_username (username),
                            unique key uk_phone (phone),
                            unique key uk_email (email),
                            key idx_role_id (role_id),
                            constraint fk_user_role
                                foreign key (role_id) references `role` (id)
                                    on update cascade
                                    on delete restrict
) engine=InnoDB default charset=utf8mb4 comment='用户表';

-- =========================
-- 5. 分类表
-- =========================
create table `category` (
                            id bigint primary key auto_increment comment '主键',
                            name varchar(50) not null comment '分类名称',
                            sort int default 0 comment '排序',
                            status tinyint default 1 comment '状态：1启用 0禁用',
                            create_time datetime default current_timestamp comment '创建时间',
                            update_time datetime default current_timestamp on update current_timestamp comment '更新时间',
                            unique key uk_category_name (name)
) engine=InnoDB default charset=utf8mb4 comment='分类表';

-- =========================
-- 6. 内容表
-- =========================
create table `content` (
                           id bigint primary key auto_increment comment '主键',
                           title varchar(100) not null comment '标题',
                           content text not null comment '内容',
                           cover_img varchar(255) default null comment '封面图',
                           category_id bigint not null comment '分类ID',
                           user_id bigint not null comment '发布者ID',
                           status tinyint default 0 comment '状态：0待审核 1已通过 2已拒绝 3已下架',
                           audit_reason varchar(255) default null comment '审核原因',
                           is_top tinyint default 0 comment '是否置顶：0否 1是',
                           view_count int default 0 comment '浏览量',
                           like_count int default 0 comment '点赞数',
                           favorite_count int default 0 comment '收藏数',
                           comment_count int default 0 comment '评论数',
                           create_time datetime default current_timestamp comment '创建时间',
                           update_time datetime default current_timestamp on update current_timestamp comment '更新时间',
                           key idx_category_id (category_id),
                           key idx_user_id (user_id),
                           key idx_status (status),
                           key idx_create_time (create_time),
                           key idx_title (title),
                           constraint fk_content_category
                               foreign key (category_id) references `category` (id)
                                   on update cascade
                                   on delete restrict,
                           constraint fk_content_user
                               foreign key (user_id) references `sys_user` (id)
                                   on update cascade
                                   on delete restrict
) engine=InnoDB default charset=utf8mb4 comment='内容表';

-- =========================
-- 7. 审核记录表
-- =========================
create table `audit_record` (
                                id bigint primary key auto_increment comment '主键',
                                content_id bigint not null comment '内容ID',
                                auditor_id bigint not null comment '审核人ID',
                                audit_result tinyint not null comment '审核结果：1通过 2拒绝',
                                audit_reason varchar(255) default null comment '审核原因',
                                create_time datetime default current_timestamp comment '创建时间',
                                key idx_content_id (content_id),
                                key idx_auditor_id (auditor_id),
                                key idx_create_time (create_time),
                                constraint fk_audit_content
                                    foreign key (content_id) references `content` (id)
                                        on update cascade
                                        on delete cascade,
                                constraint fk_audit_auditor
                                    foreign key (auditor_id) references `sys_user` (id)
                                        on update cascade
                                        on delete restrict
) engine=InnoDB default charset=utf8mb4 comment='审核记录表';

-- =========================
-- 8. 收藏表
-- =========================
create table `favorite` (
                            id bigint primary key auto_increment comment '主键',
                            user_id bigint not null comment '用户ID',
                            content_id bigint not null comment '内容ID',
                            create_time datetime default current_timestamp comment '收藏时间',
                            unique key uk_user_content (user_id, content_id),
                            key idx_user_id (user_id),
                            key idx_content_id (content_id),
                            constraint fk_favorite_user
                                foreign key (user_id) references `sys_user` (id)
                                    on update cascade
                                    on delete cascade,
                            constraint fk_favorite_content
                                foreign key (content_id) references `content` (id)
                                    on update cascade
                                    on delete cascade
) engine=InnoDB default charset=utf8mb4 comment='收藏表';

-- =========================
-- 9. 点赞表
-- =========================
create table `like_record` (
                               id bigint primary key auto_increment comment '主键',
                               user_id bigint not null comment '用户ID',
                               content_id bigint not null comment '内容ID',
                               create_time datetime default current_timestamp comment '点赞时间',
                               unique key uk_user_content_like (user_id, content_id),
                               key idx_user_id (user_id),
                               key idx_content_id (content_id),
                               constraint fk_like_user
                                   foreign key (user_id) references `sys_user` (id)
                                       on update cascade
                                       on delete cascade,
                               constraint fk_like_content
                                   foreign key (content_id) references `content` (id)
                                       on update cascade
                                       on delete cascade
) engine=InnoDB default charset=utf8mb4 comment='点赞表';

-- =========================
-- 10. 评论表
-- =========================
create table `comment` (
                           id bigint primary key auto_increment comment '主键',
                           content_id bigint not null comment '内容ID',
                           user_id bigint not null comment '用户ID',
                           parent_id bigint default null comment '父评论ID',
                           comment_text varchar(500) not null comment '评论内容',
                           status tinyint default 1 comment '状态：1正常 0删除',
                           create_time datetime default current_timestamp comment '创建时间',
                           key idx_content_id (content_id),
                           key idx_user_id (user_id),
                           key idx_parent_id (parent_id),
                           key idx_create_time (create_time),
                           constraint fk_comment_content
                               foreign key (content_id) references `content` (id)
                                   on update cascade
                                   on delete cascade,
                           constraint fk_comment_user
                               foreign key (user_id) references `sys_user` (id)
                                   on update cascade
                                   on delete cascade,
                           constraint fk_comment_parent
                               foreign key (parent_id) references `comment` (id)
                                   on update cascade
                                   on delete set null
) engine=InnoDB default charset=utf8mb4 comment='评论表';

-- =========================
-- 11. 公告表
-- =========================
create table `notice` (
                          id bigint primary key auto_increment comment '主键',
                          title varchar(100) not null comment '标题',
                          content text not null comment '内容',
                          is_top tinyint default 0 comment '是否置顶：0否 1是',
                          status tinyint default 1 comment '状态：1发布 0草稿',
                          publisher_id bigint not null comment '发布人ID',
                          create_time datetime default current_timestamp comment '创建时间',
                          update_time datetime default current_timestamp on update current_timestamp comment '更新时间',
                          key idx_publisher_id (publisher_id),
                          key idx_is_top (is_top),
                          key idx_create_time (create_time),
                          constraint fk_notice_publisher
                              foreign key (publisher_id) references `sys_user` (id)
                                  on update cascade
                                  on delete restrict
) engine=InnoDB default charset=utf8mb4 comment='公告表';

-- =========================
-- 12. 举报表
-- =========================
create table `report` (
                          id bigint primary key auto_increment comment '主键',
                          user_id bigint not null comment '举报人ID',
                          content_id bigint not null comment '被举报内容ID',
                          report_type varchar(50) not null comment '举报类型',
                          report_reason varchar(255) not null comment '举报原因',
                          status tinyint default 0 comment '状态：0待处理 1已处理',
                          handler_id bigint default null comment '处理人ID',
                          handle_result varchar(255) default null comment '处理结果',
                          create_time datetime default current_timestamp comment '创建时间',
                          handle_time datetime default null comment '处理时间',
                          key idx_user_id (user_id),
                          key idx_content_id (content_id),
                          key idx_status (status),
                          key idx_create_time (create_time),
                          key idx_handler_id (handler_id),
                          constraint fk_report_user
                              foreign key (user_id) references `sys_user` (id)
                                  on update cascade
                                  on delete cascade,
                          constraint fk_report_content
                              foreign key (content_id) references `content` (id)
                                  on update cascade
                                  on delete cascade,
                          constraint fk_report_handler
                              foreign key (handler_id) references `sys_user` (id)
                                  on update cascade
                                  on delete set null
) engine=InnoDB default charset=utf8mb4 comment='举报表';

-- =========================
-- 13. 消息表
-- =========================
create table `message` (
                           id bigint primary key auto_increment comment '主键',
                           user_id bigint not null comment '接收用户ID',
                           title varchar(100) not null comment '消息标题',
                           content varchar(500) not null comment '消息内容',
                           type varchar(50) not null comment '消息类型',
                           is_read tinyint default 0 comment '是否已读：0未读 1已读',
                           related_id bigint default null comment '关联业务ID',
                           create_time datetime default current_timestamp comment '创建时间',
                           key idx_user_id (user_id),
                           key idx_is_read (is_read),
                           key idx_type (type),
                           key idx_create_time (create_time),
                           constraint fk_message_user
                               foreign key (user_id) references `sys_user` (id)
                                   on update cascade
                                   on delete cascade
) engine=InnoDB default charset=utf8mb4 comment='消息表';

-- =========================
-- 14. 初始化角色数据
-- =========================
insert into `role` (role_name, role_code, status) values
                                                      ('管理员', 'ADMIN', 1),
                                                      ('普通用户', 'USER', 1);

-- =========================
-- 15. 初始化分类数据
-- =========================
insert into `category` (name, sort, status) values
                                                ('失物招领', 1, 1),
                                                ('二手交易', 2, 1),
                                                ('活动通知', 3, 1),
                                                ('校园求助', 4, 1),
                                                ('课程资料', 5, 1),
                                                ('其他服务', 6, 1);
