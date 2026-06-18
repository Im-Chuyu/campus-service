# Database Scripts

这个目录保存校园服务平台的数据库脚本。

## 文件说明

- `campus.sql`：完整建库脚本。会删除并重建 `campus_service` 数据库，适合本地开发、测试环境、演示环境。
- `maintenance.sql`：维护脚本集合。包含管理员授权、分类维护、系统配置、内容状态调整、数据检查等常用 SQL。
- `fix-activity-sub-category.sql`：活动通知子分类相关修复脚本。
- `optimize-private-chat.sql`：私聊相关优化脚本。
- `REBUILD.md`：测试环境删库重建说明。

## 重要提醒

`campus.sql` 会执行删库重建。生产环境不要直接执行，除非已经确认可以清空数据。

测试环境执行：

```bash
mysql -uroot -p < campus.sql
```

服务器部署时常见路径：

```bash
mysql -uroot -p < /opt/campus-service/database/campus.sql
```

## 管理员初始化

建库脚本不会内置默认管理员账号。推荐：

1. 先从前端注册用户。
2. 再执行维护 SQL，把目标用户设置为管理员。

示例：

```sql
USE campus_service;
UPDATE sys_user
SET role_id = 1,
    status = 1,
    update_time = NOW()
WHERE username = 'admin';
```

最高权限管理员建议用户名为 `admin`，且满足：

```text
username = 'admin'
role_id = 1
status = 1
```
