# 测试环境重建数据库

`campus.sql` 是完整建库脚本，适合测试阶段删库重建。它会先删除 `campus_service` 数据库，再重新创建表、外键、基础分类和系统配置。

服务器上执行：

```bash
mysql -uroot -p < /opt/campus-service/campus-sql/campus.sql
```

如果只是升级旧库，不删数据，则不要执行 `campus.sql`，按需执行增量脚本，例如 `optimize-private-chat.sql`。

建库后先注册一个用户，再用 `maintenance.sql` 里的管理员维护语句把该账号改成管理员。最高权限管理员仍按用户名 `admin` 判断，所以需要最高权限时请确保该账号 `username = 'admin'`、`role_id = 1`、`status = 1`。
