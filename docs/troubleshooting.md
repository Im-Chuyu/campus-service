# 故障排查

## 后端服务没有启动

查看服务状态：

```bash
systemctl status campus-service --no-pager -l
journalctl -u campus-service -n 120 --no-pager
```

常见原因：

- JDK 版本不是 17+
- `application-prod.yml` 数据库密码错误
- MySQL 没启动
- Redis 开启但 Redis 服务不可用
- jar 文件路径不对

## 接口返回未登录

现象：

```json
{"code":401,"msg":"未登录","data":null}
```

排查：

- 前端是否已登录
- 请求头是否带 `Authorization`
- 是否刚修改密码或重新登录导致旧 token 失效
- Redis 登录设备校验开启后，Redis 中是否还有登录 token

## 前端页面空白

排查：

```bash
curl -I http://127.0.0.1/
nginx -t
```

检查：

- `frontend/dist/index.html` 是否存在
- Nginx `root` 是否指向 `/opt/campus-service/frontend/dist`
- Vue Router 是否配置了 `try_files $uri $uri/ /index.html`

## 本地前端无法启动

先确认在前端目录：

```bash
cd /d/java-project/campus-service/frontend
```

检查脚本：

```bash
npm run
```

正常应能看到 `dev`、`build`、`preview`。如果依赖缺失，重新安装：

```bash
npm install
npm run dev
```

如果端口被占用，Vite 会自动换端口，按终端输出的地址访问即可。

## `/api` 请求失败

排查：

```bash
curl -i http://127.0.0.1/api/
curl -i http://127.0.0.1:8080/
```

如果 8080 正常但 `/api` 不正常，问题通常在 Nginx 代理配置。

## 上传图片后无法访问

检查上传目录：

```bash
ls -lh /opt/campus-service/uploads
```

检查 Nginx：

```nginx
location /uploads/ {
    proxy_pass http://127.0.0.1:8080/uploads/;
}
```

检查后端配置：

```yaml
campus:
  upload:
    path: /opt/campus-service/uploads
```

## 修改资料提示系统异常

先看错误日志：

```bash
tail -n 120 /opt/campus-service/logs/app-error.log
```

如果出现唯一索引冲突，例如 `Duplicate entry '' for key 'sys_user.uk_email'`，说明旧数据里可能有空字符串，需要清理：

```bash
mysql -uroot -p -e "
USE campus_service;
UPDATE sys_user SET email = NULL WHERE email = '';
UPDATE sys_user SET phone = NULL WHERE phone = '';
"
```

## 新用户第一次进入个人主页异常

先看错误日志：

```bash
tail -n 120 /opt/campus-service/logs/app-error.log
```

如果出现 `Deadlock found when trying to get lock` 并且堆栈指向 `FriendCategoryMapper.insertIgnore`，说明默认好友分类初始化发生并发冲突。当前代码已经对同一用户初始化加锁，更新后端 jar 并重启服务即可。

## Redis 检查

```bash
redis-cli ping
```

返回：

```text
PONG
```

后端是否开启 Redis：

```bash
grep -n "redis-enabled" /opt/campus-service/config/application-prod.yml
```

## 首页大卡片是否频繁查库

首页进入时前端会请求：

```text
GET /home/hero/public
```

后端当前已经加入 5 分钟内存缓存，普通刷新不会每次都访问数据库。管理员在后台保存首页大卡片后，缓存会立即刷新。

如果想确认接口是否正常：

```bash
curl -i http://127.0.0.1:8080/home/hero/public
```

## MySQL 检查

```bash
systemctl status mysql --no-pager
mysql -uroot -p -e "USE campus_service; SHOW TABLES;"
```

## Nginx 检查

```bash
nginx -t
systemctl status nginx --no-pager
tail -n 100 /var/log/nginx/error.log
```

## 快速回滚思路

如果更新后端失败：

1. 停止服务。
2. 把旧 jar 拷回 `/opt/campus-service/backend/campus-service.jar`。
3. 重启 `campus-service`。

```bash
systemctl stop campus-service
cp /path/to/old/campus-service.jar /opt/campus-service/backend/campus-service.jar
systemctl start campus-service
systemctl status campus-service --no-pager -l
```
