# 测试环境重建数据库

`campus.sql` 是完整建库脚本，适合测试阶段删库重建。

它会：

1. 删除旧的 `campus_service` 数据库。
2. 重新创建数据库和表。
3. 初始化基础角色、分类和系统配置。

## 执行命令

在项目本地：

```bash
mysql -uroot -p < database/campus.sql
```

在服务器上：

```bash
mysql -uroot -p < /opt/campus-service/database/campus.sql
```

## 重建后的操作

1. 启动后端和前端。
2. 在前端注册管理员账号，建议用户名为 `admin`。
3. 使用 `maintenance.sql` 中的管理员维护 SQL 提权。

示例：

```sql
USE campus_service;
UPDATE sys_user
SET role_id = 1,
    status = 1,
    update_time = NOW()
WHERE username = 'admin';
```

## 不删库升级

如果只是升级旧库，不要执行 `campus.sql`。应该按需要执行增量脚本，例如：

```text
fix-activity-sub-category.sql
optimize-private-chat.sql
maintenance.sql 中的指定片段
```
