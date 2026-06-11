# Campus Service 校园服务平台

Campus Service 是一个前后端分离的校园服务平台，包含内容发布、分类管理、评论互动、点赞收藏、好友关系、私聊、公告、举报审核、管理员后台、首页大卡片配置、个人主页资料卡等功能。

这个仓库同时包含后端、前端和数据库脚本，适合直接作为完整项目部署。

## 功能概览

- 用户账号：注册、登录、JWT 鉴权、密码修改和重置、资料编辑、头像和资料卡背景上传
- 内容服务：内容发布、编辑、删除、公开/私密内容、分类筛选、浏览量、点赞、收藏
- 评论互动：评论、回复、评论反应、评论置顶
- 好友和私聊：好友分类、好友列表、屏蔽用户、私聊会话、未读消息
- 公告和通知：首页公告、公告详情、系统消息、私聊消息提醒
- 管理后台：用户管理、内容审核、分类管理、举报处理、公告管理、数据统计
- 首页配置：管理员维护首页大卡片展示内容
- Redis 能力：登录设备校验、登录失败限流、热门内容缓存、点赞/浏览计数缓冲

## 技术栈

后端：

- JDK 17
- Spring Boot 3.3.5
- MyBatis
- MySQL
- Redis
- JWT
- Maven

前端：

- Vue 3
- Vite
- Pinia
- Vue Router
- Element Plus
- Axios

部署：

- Ubuntu 22.04
- systemd
- Nginx
- MySQL
- Redis

## 项目结构

```text
.
├── src/                         # Spring Boot 后端源码
├── campus-service-web/          # Vue 前端源码
├── campus-sql/                  # 建库、重建、维护 SQL
├── docs/                        # 部署和排查文档
├── pom.xml                      # 后端 Maven 配置
├── .gitignore                   # 仓库忽略规则
├── .gitattributes               # 换行和文本规则
└── README.md
```

## 环境要求

本地开发建议：

- JDK 17 或更高版本
- Maven 3.8+
- Node.js 18+ 或 20+
- MySQL 8.x
- Redis 6.x 或更高版本，可选

生产部署建议：

- Ubuntu 22.04 LTS
- OpenJDK 17
- Nginx
- MySQL
- Redis

## 快速启动

### 1. 初始化数据库

`campus-sql/campus.sql` 是完整建库脚本，会删除并重建 `campus_service` 数据库，只适合本地开发、测试环境或确认允许清空数据的场景。

```bash
mysql -uroot -p < campus-sql/campus.sql
```

### 2. 启动后端

后端默认读取 `src/main/resources/application.yml`，常用配置可以用环境变量覆盖。

```bash
mvn spring-boot:run
```

常用环境变量：

```text
SERVER_PORT=8080
DB_URL=jdbc:mysql://localhost:3306/campus_service?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&connectionTimeZone=Asia/Shanghai
DB_USERNAME=root
DB_PASSWORD=your_mysql_password
REDIS_HOST=localhost
REDIS_PORT=6379
CAMPUS_UPLOAD_PATH=uploads
CAMPUS_CORS_ALLOWED_ORIGINS=http://localhost:5173,http://127.0.0.1:5173
CAMPUS_REDIS_ENABLED=false
JWT_SECRET=replace_with_at_least_32_chars_secret
```

### 3. 启动前端

```bash
cd campus-service-web
npm install
npm run dev
```

前端开发服务默认监听 `5173`，并通过 Vite 将 `/api` 代理到后端。配置示例：

```text
campus-service-web/.env.example
```

## 构建

后端构建：

```bash
mvn clean package
```

构建产物：

```text
target/campus-service-1.0-SNAPSHOT.jar
```

前端构建：

```bash
cd campus-service-web
npm ci
npm run build
```

构建产物：

```text
campus-service-web/dist/
```

## 生产配置

生产配置模板：

```text
src/main/resources/application-prod.example.yml
```

服务器上建议复制为：

```text
/opt/campus-service/config/application-prod.yml
```

不要把真实生产配置、数据库密码、JWT 密钥提交到 GitHub。

## 部署文档

完整部署步骤见：

- [docs/deploy.md](docs/deploy.md)
- [docs/troubleshooting.md](docs/troubleshooting.md)

## 管理员初始化

建库脚本不会内置默认管理员账号。推荐流程：

1. 启动前后端。
2. 在前端注册一个用户，最高权限管理员建议用户名使用 `admin`。
3. 使用 `campus-sql/maintenance.sql` 中的管理员维护 SQL，将该用户设置为管理员。

示例：

```sql
USE campus_service;
UPDATE sys_user
SET role_id = 1,
    status = 1,
    update_time = NOW()
WHERE username = 'admin';
```

说明：部分最高权限操作会按 `username = 'admin'` 判断，因此最高权限管理员建议保留用户名 `admin`。

## SQL 脚本说明

- `campus-sql/campus.sql`：完整建库脚本，会删除并重建数据库
- `campus-sql/maintenance.sql`：常用维护 SQL，例如管理员授权、分类维护、数据检查
- `campus-sql/fix-activity-sub-category.sql`：活动通知子分类相关修复脚本
- `campus-sql/optimize-private-chat.sql`：私聊相关优化脚本
- `campus-sql/REBUILD.md`：测试环境重建数据库说明

## Git 提交约定

建议不要提交以下内容：

- `target/`
- `campus-service-web/node_modules/`
- `campus-service-web/dist/`
- `logs/`
- `uploads/`
- `.idea/`
- `.vscode/`
- `.env`
- `application-prod.yml`
- 任何真实密码、Token、密钥

## 常见问题入口

接口返回未登录：

- 检查前端是否携带 `Authorization`
- 检查 Redis 登录设备校验是否开启
- 检查用户是否重新登录过

上传图片无法访问：

- 检查 `CAMPUS_UPLOAD_PATH`
- 检查 Nginx `/uploads/` 代理
- 检查服务器文件权限

更多排查命令见 [docs/troubleshooting.md](docs/troubleshooting.md)。
