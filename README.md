# Campus Service

校园服务平台，包含 Spring Boot 后端、Vue 前端和数据库脚本。

## 项目结构

```text
.
├── src/                         # 后端源码
├── campus-sql/                  # 数据库建库和维护脚本
├── campus-service-web/          # 前端源码
├── pom.xml                      # 后端 Maven 配置
└── README.md
```

## 技术栈

- 后端：Spring Boot 3.3、MyBatis、MySQL、Redis、JWT
- 前端：Vue 3、Vite、Pinia、Element Plus、Axios
- 部署：Ubuntu、systemd、Nginx、MySQL、Redis

## 后端本地运行

后端需要 JDK 17+、Maven、MySQL。

```bash
mvn spring-boot:run
```

默认配置读取环境变量，常用项：

```text
SERVER_PORT=8080
DB_URL=jdbc:mysql://localhost:3306/campus_service?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&connectionTimeZone=Asia/Shanghai
DB_USERNAME=root
DB_PASSWORD=
CAMPUS_UPLOAD_PATH=uploads
CAMPUS_REDIS_ENABLED=false
```

生产配置可以参考：

```text
src/main/resources/application-prod.example.yml
```

## 前端本地运行

```bash
cd campus-service-web
npm install
npm run dev
```

前端开发环境通过 Vite 将 `/api` 代理到后端，配置示例见：

```text
campus-service-web/.env.example
```

## 数据库

完整重建测试库：

```bash
mysql -uroot -p < campus-sql/campus.sql
```

`campus.sql` 会删除并重建 `campus_service` 数据库，只适合测试环境或确认允许清空数据的场景。

## 构建

后端：

```bash
mvn clean package
```

前端：

```bash
cd campus-service-web
npm run build
```

更多部署步骤见 [docs/deploy.md](docs/deploy.md)。
