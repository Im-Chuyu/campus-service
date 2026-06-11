# 部署说明

本文档记录 Campus Service 在 Ubuntu ECS 上的完整部署流程。示例路径统一使用：

```text
/opt/campus-service
```

## 目录规划

```text
/opt/campus-service
├── backend/                  # 后端 jar
│   └── campus-service.jar
├── config/                   # 生产配置
│   └── application-prod.yml
├── frontend/                 # 前端静态文件
│   └── dist/
├── logs/                     # 后端日志
├── sql/                      # SQL 脚本
└── uploads/                  # 用户上传文件
```

创建目录：

```bash
mkdir -p /opt/campus-service/{backend,config,frontend,logs,sql,uploads}
```

## 服务器依赖

安装 JDK、Nginx、MySQL、Redis：

```bash
apt update
apt install -y openjdk-17-jdk nginx mysql-server redis-server unzip
```

检查版本和服务：

```bash
java -version
nginx -v
mysql --version
redis-cli ping
```

`redis-cli ping` 正常应返回：

```text
PONG
```

## 数据库初始化

测试环境可以直接使用完整建库脚本：

```bash
mysql -uroot -p < /opt/campus-service/sql/campus.sql
```

注意：`campus.sql` 会删除并重建 `campus_service` 数据库。生产环境不要直接执行这个脚本，应使用增量脚本或先备份数据。

创建业务数据库用户示例：

```sql
CREATE USER 'campus_user'@'localhost' IDENTIFIED BY 'replace_with_strong_password';
GRANT ALL PRIVILEGES ON campus_service.* TO 'campus_user'@'localhost';
FLUSH PRIVILEGES;
```

## 后端配置

将模板复制为生产配置：

```bash
cp src/main/resources/application-prod.example.yml /opt/campus-service/config/application-prod.yml
```

生产配置示例：

```yaml
server:
  port: 8080
  address: 127.0.0.1

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campus_service?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&connectionTimeZone=Asia/Shanghai
    username: campus_user
    password: ${DB_PASSWORD}

  data:
    redis:
      host: localhost
      port: 6379
      database: 0
      timeout: 3000ms

campus:
  upload:
    path: /opt/campus-service/uploads
  cors:
    allowed-origins: http://your-domain-or-server-ip
  cache:
    redis-enabled: true
    hot-content-ttl-seconds: 60
    login-rate-limit-window-seconds: 60
    login-rate-limit-max-attempts: 10
    counter-flush-delay-ms: 30000
    like-write-behind-enabled: false
    view-write-behind-enabled: false
```

建议把数据库密码和 JWT 密钥放到 systemd 环境变量，不要写死在仓库文件里。

## 后端构建和部署

本地构建：

```bash
mvn clean package
```

上传并替换服务器 jar：

```bash
cp target/campus-service-1.0-SNAPSHOT.jar /opt/campus-service/backend/campus-service.jar
```

systemd 服务文件：

```bash
vim /etc/systemd/system/campus-service.service
```

内容示例：

```ini
[Unit]
Description=Campus Service Spring Boot Application
After=network.target mysql.service redis-server.service

[Service]
Type=simple
WorkingDirectory=/opt/campus-service
Environment=DB_PASSWORD=replace_with_mysql_password
Environment=JWT_SECRET=replace_with_at_least_32_chars_secret
ExecStart=/usr/bin/java -jar /opt/campus-service/backend/campus-service.jar --spring.config.additional-location=/opt/campus-service/config/application-prod.yml
Restart=on-failure
RestartSec=5

[Install]
WantedBy=multi-user.target
```

启动：

```bash
systemctl daemon-reload
systemctl enable campus-service
systemctl restart campus-service
systemctl status campus-service --no-pager -l
```

后端本机检查：

```bash
curl -i http://127.0.0.1:8080/
```

如果返回业务 JSON，例如 `未登录`，说明后端已经正常响应。

## 前端构建和部署

本地构建：

```bash
cd campus-service-web
npm ci
npm run build
```

部署到服务器：

```bash
rm -rf /opt/campus-service/frontend/dist
mkdir -p /opt/campus-service/frontend/dist
cp -a campus-service-web/dist/. /opt/campus-service/frontend/dist/
```

前端生产环境默认请求 `/api`，需要由 Nginx 转发到后端。

## Nginx 配置

示例：

```nginx
server {
    listen 80;
    server_name your-domain-or-server-ip;

    root /opt/campus-service/frontend/dist;
    index index.html;

    client_max_body_size 2048m;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /uploads/ {
        proxy_pass http://127.0.0.1:8080/uploads/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

检查并重载：

```bash
nginx -t
systemctl reload nginx
```

访问检查：

```bash
curl -I http://127.0.0.1/
curl -i http://127.0.0.1/api/
```

## 管理员设置

建库后先在前端注册用户，再执行 SQL 授权。

```bash
mysql -uroot -p -e "
USE campus_service;
UPDATE sys_user
SET role_id = 1,
    status = 1,
    update_time = NOW()
WHERE username IN ('admin', 'another_admin');

SELECT id, username, role_id, status
FROM sys_user
WHERE username IN ('admin', 'another_admin');
"
```

最高权限管理员建议用户名为 `admin`，因为部分最高权限操作会按用户名判断。

## Redis 开关

生产配置中：

```yaml
campus:
  cache:
    redis-enabled: true
```

开启后需要重启后端：

```bash
systemctl restart campus-service
redis-cli ping
systemctl status campus-service --no-pager -l
```

如果 Redis 暂时不可用，可以先关闭：

```yaml
redis-enabled: false
```

## 更新发布流程

后端更新：

```bash
mvn clean package
cp target/campus-service-1.0-SNAPSHOT.jar /opt/campus-service/backend/campus-service.jar
systemctl restart campus-service
```

前端更新：

```bash
cd campus-service-web
npm ci
npm run build
rm -rf /opt/campus-service/frontend/dist
mkdir -p /opt/campus-service/frontend/dist
cp -a dist/. /opt/campus-service/frontend/dist/
nginx -t
systemctl reload nginx
```

SQL 更新：

- 测试环境可以删库重建。
- 生产环境使用增量脚本。
- 执行前先备份数据库。

## 验证清单

```bash
systemctl status campus-service --no-pager -l
journalctl -u campus-service -n 80 --no-pager
tail -n 80 /opt/campus-service/logs/app.log
tail -n 80 /opt/campus-service/logs/app-error.log
nginx -t
curl -I http://127.0.0.1/
curl -i http://127.0.0.1/api/
redis-cli ping
```

浏览器检查：

- 首页能打开
- 注册、登录正常
- 上传头像、资料卡背景正常
- 内容发布、评论、点赞、收藏正常
- 管理员后台能进入
- Nginx `/api` 和 `/uploads` 代理正常
