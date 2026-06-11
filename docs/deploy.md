# 部署说明

以下说明基于 Ubuntu 22.04、JDK 17、Nginx、MySQL、Redis。

## 后端

构建：

```bash
mvn clean package
```

上传并替换服务器 jar：

```bash
cp target/campus-service-1.0-SNAPSHOT.jar /opt/campus-service/backend/campus-service.jar
systemctl restart campus-service
systemctl status campus-service --no-pager -l
```

systemd 服务示例：

```ini
[Unit]
Description=Campus Service Spring Boot Application
After=network.target

[Service]
WorkingDirectory=/opt/campus-service
ExecStart=/usr/bin/java -jar /opt/campus-service/backend/campus-service.jar --spring.config.additional-location=/opt/campus-service/config/application-prod.yml
Restart=on-failure

[Install]
WantedBy=multi-user.target
```

## 前端

构建：

```bash
cd campus-service-web
npm install
npm run build
```

部署到 Nginx 静态目录：

```bash
rm -rf /opt/campus-service/frontend/dist
mkdir -p /opt/campus-service/frontend/dist
cp -a campus-service-web/dist/. /opt/campus-service/frontend/dist/
nginx -t
systemctl reload nginx
```

## Nginx 示例

```nginx
server {
    listen 80;
    server_name your-domain-or-server-ip;

    root /opt/campus-service/frontend/dist;
    index index.html;

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
    }
}
```

## 数据库

测试环境删库重建：

```bash
mysql -uroot -p < /opt/campus-service/sql/campus.sql
```

生产环境不要直接执行 `campus.sql`，应使用增量脚本或手工确认数据迁移。

## 常用排查命令

```bash
systemctl status campus-service --no-pager -l
journalctl -u campus-service -n 100 --no-pager
tail -n 100 /opt/campus-service/logs/app.log
tail -n 100 /opt/campus-service/logs/app-error.log
redis-cli ping
nginx -t
```
