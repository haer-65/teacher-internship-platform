# 师范生教育实习全过程管理平台

这是一个面向师范生教育实习场景的全过程管理平台，覆盖登录认证、实习计划发布、学生申请与分配、过程材料提交、指导评价、成绩计算发布、站内通知、统计分析和系统管理等完整链路。

平台按角色协同工作：
- 学生查看已发布计划，提交实习申请，接收分配结果，上传过程材料，查看评价与成绩。
- 院系管理员和教务管理员负责计划维护、申请审核、分配管理、消息通知和基础数据维护。
- 校内指导教师和基地指导教师负责过程评价、综合评价和成绩确认。
- 系统管理员负责用户、角色、菜单、参数、日志和基础字典配置。

## 技术栈

- 前端：Vue 3、TypeScript、Vite、Element Plus、Pinia、Vue Router、Axios、ECharts
- 后端：Spring Boot 2.7、Spring Security、JWT、MyBatis-Plus
- 数据库：MySQL 8.0
- 缓存：Redis 6.x
- 接口文档：Swagger / Knife4j
- 本地文件：`uploads/`

## 核心流程

1. 用户登录并获取角色上下文，支持角色切换。
2. 管理员创建并发布实习计划，配置面向学生开放的计划信息和材料要求。
3. 学生提交申请，管理员审核并完成人工/批量分配。
4. 学生根据分配结果上传实习材料，教师按材料和最终表现开展评价。
5. 系统按计划权重计算成绩，支持人工调整、发布和导出。
6. 通知模块同步关键节点消息，统计模块输出过程和结果数据。

## 目录结构

```text
teacher-internship-platform/
├─ backend/      # Spring Boot 后端
├─ frontend/     # Vue3 前端
├─ sql/          # 数据库脚本与迁移脚本副本
├─ docs/         # 交付文档
├─ tools/        # Windows 本地依赖与启动脚本
├─ uploads/      # 文件上传目录
└─ README.md
```

## 快速启动

### 方式一：一键启动

在项目根目录执行：

```powershell
.\tools\start-project.ps1
```

脚本会自动：
- 检查并启动 MySQL 和 Redis
- 按需加载本地 Maven 与离线仓库
- 启动后端服务
- 启动前端开发服务

### 方式二：手动启动

先准备依赖，再分别启动前后端：

```powershell
.\tools\start-deps.ps1
cd .\backend
mvn spring-boot:run
```

另开终端启动前端：

```powershell
cd .\frontend
npm install
npm run dev
```

默认访问地址：
- 前端：`http://127.0.0.1:5173`
- 后端：`http://127.0.0.1:8080`
- 健康检查：`http://127.0.0.1:8080/api/v1/health/check`

## 配置说明

后端默认读取以下环境变量：
- `DB_HOST` `DB_PORT` `DB_NAME` `DB_USERNAME` `DB_PASSWORD`
- `REDIS_HOST` `REDIS_PORT` `REDIS_DB` `REDIS_PASSWORD`
- `JWT_SECRET` `JWT_EXPIRE_SECONDS`
- `FILE_STORAGE_ROOT` `FILE_MAX_SIZE_MB`
- `APP_BOOTSTRAP_ENABLED` `APP_BOOTSTRAP_SCRIPT_LOCATION`

本地工具脚本默认会把文件存储路径指向仓库下的 `uploads/`，并把运行时日志写到系统临时目录中。

## 默认账号

见 [默认账号说明](./docs/05-default-accounts.md)。

## API 文档

- Knife4j：`http://localhost:8080/doc.html`
- Swagger UI：`http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON：`http://localhost:8080/v3/api-docs`

见 [接口文档说明](./docs/06-api-doc-guide.md)。

## 常用脚本

- 启动依赖：`.\tools\start-deps.ps1`
- 启动项目：`.\tools\start-project.ps1`
- 停止项目：`.\tools\stop-project.ps1`
- 健康检查：`.\tools\check-deps.ps1`

## 文档索引

- [01-工程拆分与阶段计划](./docs/01-工程拆分与阶段计划.md)
- [02-ER关系说明](./docs/02-ER关系说明.md)
- [03-database-init](./docs/03-database-init.md)
- [04-windows-deploy-guide](./docs/04-windows-deploy-guide.md)
- [05-default-accounts](./docs/05-default-accounts.md)
- [06-api-doc-guide](./docs/06-api-doc-guide.md)
- [07-demo-script](./docs/07-demo-script.md)
- [08-project-structure](./docs/08-project-structure.md)
- [09-delivery-checklist](./docs/09-delivery-checklist.md)
- [10-local-deps-quick-start](./docs/10-local-deps-quick-start.md)

