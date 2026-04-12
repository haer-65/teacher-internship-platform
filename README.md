# 师范生教育实习全过程管理平台

面向师范生、校内指导教师、实习基地指导教师、院系管理员、教务处管理员、系统管理员的 Web 管理平台。  
核心业务闭环：计划发布 -> 学生申请 -> 分配落实 -> 材料提交 -> 指导评价 -> 成绩计算与发布 -> 统计分析。

## 1. 技术栈

- 前端：Vue 3 + TypeScript + Vite + Element Plus + Pinia + Vue Router + Axios + ECharts
- 后端：Spring Boot 2.7 + Spring Security + JWT + MyBatis-Plus
- 数据库：MySQL 8.0
- 缓存：Redis 6.0
- 文档：Swagger / Knife4j
- 文件存储：Windows 本地目录（通过后端鉴权访问）

## 2. 环境要求

| 组件 | 建议版本 |
|---|---|
| Windows | Windows 10/11 x64 |
| Node.js | 18.x 或 20.x |
| npm | 9.x+ |
| JDK | 17 |
| Maven | 3.8+ |
| MySQL | 8.0 |
| Redis | 6.0+ |

## 3. 项目目录

```text
teacher-internship-platform/
├─ frontend/                 # 前端工程
├─ backend/                  # 后端工程
├─ sql/                      # 数据库初始化与增量脚本
├─ docs/                     # 交付文档
├─ uploads/                  # 文件存储根目录（默认）
└─ temp/                     # 临时文件目录
```

## 4. 快速启动（本地开发）

### 4.1 数据库初始化

开发环境默认支持自动初始化：

- 自动建库：`createDatabaseIfNotExist=true`
- 自动执行脚本：`backend/src/main/resources/db/bootstrap/V1~V9`
- 开关配置：`APP_BOOTSTRAP_ENABLED=true`

如果你希望手工初始化，也可以按顺序执行以下 SQL 脚本：

1. `sql/V1__init_schema.sql`
2. `sql/V2__seed_data.sql`
3. `sql/V3__seed_plan_data.sql`
4. `sql/V4__seed_application_rbac.sql`
5. `sql/V5__material_management_ext.sql`
6. `sql/V6__evaluation_management_ext.sql`
7. `sql/V7__score_management_ext.sql`
8. `sql/V8__stats_analysis_ext.sql`
9. `sql/V9__menu_component_path_alignment.sql`

示例（PowerShell）：

```powershell
mysql -h 127.0.0.1 -P 3306 -u root -p < .\sql\V1__init_schema.sql
mysql -h 127.0.0.1 -P 3306 -u root -p teacher_internship_platform < .\sql\V2__seed_data.sql
mysql -h 127.0.0.1 -P 3306 -u root -p teacher_internship_platform < .\sql\V3__seed_plan_data.sql
mysql -h 127.0.0.1 -P 3306 -u root -p teacher_internship_platform < .\sql\V4__seed_application_rbac.sql
mysql -h 127.0.0.1 -P 3306 -u root -p teacher_internship_platform < .\sql\V5__material_management_ext.sql
mysql -h 127.0.0.1 -P 3306 -u root -p teacher_internship_platform < .\sql\V6__evaluation_management_ext.sql
mysql -h 127.0.0.1 -P 3306 -u root -p teacher_internship_platform < .\sql\V7__score_management_ext.sql
mysql -h 127.0.0.1 -P 3306 -u root -p teacher_internship_platform < .\sql\V8__stats_analysis_ext.sql
mysql -h 127.0.0.1 -P 3306 -u root -p teacher_internship_platform < .\sql\V9__menu_component_path_alignment.sql
```

### 4.2 Redis 启动

方式 A（已注册服务）：

```powershell
net start Redis
```

方式 B（手动启动）：

```powershell
redis-server.exe
```

### 4.3 后端配置与启动

后端主要配置文件：

- `backend/src/main/resources/application.yml`
- `backend/src/main/resources/application-dev.yml`
- 环境变量示例：`backend/.env.example`

关键配置项：

- 数据库：`DB_HOST` `DB_PORT` `DB_NAME` `DB_USERNAME` `DB_PASSWORD`
- Redis：`REDIS_HOST` `REDIS_PORT` `REDIS_DB` `REDIS_PASSWORD`
- JWT：`JWT_SECRET` `JWT_EXPIRE_SECONDS`
- 文件：`FILE_STORAGE_ROOT` `FILE_MAX_SIZE_MB`
- 启动初始化：`APP_BOOTSTRAP_ENABLED` `APP_BOOTSTRAP_SCRIPT_LOCATION`

启动命令：

```powershell
cd .\backend
mvn spring-boot:run
```

启动后访问：

- 后端接口基地址：`http://localhost:8080/api`
- 健康检查：`http://localhost:8080/api/v1/health/check`

### 4.4 前端配置与启动

前端环境文件示例：`frontend/.env.example`（开发建议使用 `frontend/.env.development`）

```env
VITE_APP_TITLE=师范生教育实习全过程管理平台
VITE_API_BASE_URL=/api
VITE_BACKEND_TARGET=http://localhost:8080
```

开发启动：

```powershell
cd .\frontend
npm install
npm run dev
```

前端默认地址：`http://localhost:5173`

## 5. 启动命令总表

| 场景 | 命令 |
|---|---|
| 启动 Redis（服务） | `net start Redis` |
| 启动 Redis（手动） | `redis-server.exe` |
| 启动后端（开发） | `cd .\backend && mvn spring-boot:run` |
| 启动前端（开发） | `cd .\frontend && npm install && npm run dev` |
| 前端打包 | `cd .\frontend && npm run build` |
| 后端打包 | `cd .\backend && mvn clean package -DskipTests` |
| 运行后端 Jar | `cd .\backend && java -jar .\target\internship-platform-backend-0.1.0.jar` |

## 6. 打包方式

### 6.1 前端打包

```powershell
cd .\frontend
npm run build
```

产物目录：`frontend/dist`

### 6.2 后端打包

```powershell
cd .\backend
mvn clean package -DskipTests
```

产物目录：`backend/target`，运行示例：

```powershell
java -jar .\target\internship-platform-backend-0.1.0.jar
```

## 7. 默认账号（初始化后）

默认密码均为：`123456`

- `A0001`（系统管理员，工号登录）
- `D0001`（院系管理员，工号登录）
- `J0001`（教务处管理员，工号登录）
- `T0001`（校内指导教师，工号登录）
- `B0001`（基地指导教师，工号登录）
- `20230001`（师范生）
- `20230002`（师范生）

详细说明见：[默认账号说明](./docs/05-default-accounts.md)

## 8. 接口文档访问

后端启动后可访问：

- Knife4j：`http://localhost:8080/doc.html`
- Swagger UI：`http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON：`http://localhost:8080/v3/api-docs`

详细说明见：[接口文档访问说明](./docs/06-api-doc-guide.md)

## 9. 文件目录配置

默认文件目录：`D:/teacher-internship-platform/uploads`

修改方式：

1. 配置环境变量 `FILE_STORAGE_ROOT`
2. 或修改 `backend/src/main/resources/application.yml` 中 `app.file-storage.root-path`

注意：

- 目录建议使用非系统盘固定路径。
- 上传、预览、下载均通过后端权限接口，禁止直接暴露物理目录。

## 10. 交付文档索引

- [数据库初始化说明](./docs/03-database-init.md)
- [Windows 本地部署手册](./docs/04-windows-deploy-guide.md)
- [默认账号说明](./docs/05-default-accounts.md)
- [接口文档访问说明](./docs/06-api-doc-guide.md)
- [演示流程文档](./docs/07-demo-script.md)
- [项目目录说明文档](./docs/08-project-structure.md)
- [最终交付清单](./docs/09-delivery-checklist.md)
