# Windows 本地部署手册（实训答辩版）

## 1. 部署目标

在一台 Windows 机器上完成前后端本地部署，支持课堂答辩展示主流程。

## 2. 环境准备清单

| 软件 | 建议版本 | 用途 |
|---|---|---|
| JDK | 17 | 后端运行 |
| Maven | 3.8+ | 后端构建与启动 |
| Node.js | 18/20 | 前端运行与打包 |
| npm | 9+ | 前端依赖管理 |
| MySQL | 8.0 | 业务数据存储 |
| Redis | 6.0+ | 缓存与参数读取 |

## 3. 推荐目录结构

```text
D:\teacher-internship-platform\
├─ backend
├─ frontend
├─ sql
├─ docs
├─ uploads
└─ temp
```

## 4. 部署步骤

### 4.1 初始化数据库

按顺序执行 `sql/V1` 到 `sql/V9`。  
详见：[数据库初始化说明](./03-database-init.md)。

### 4.2 启动 Redis

方式 A（服务）：

```powershell
net start Redis
```

方式 B（命令行）：

```powershell
redis-server.exe
```

### 4.3 配置后端

检查 `backend/src/main/resources/application.yml` 与 `application-dev.yml`，重点项：

- 数据源地址与账号
- Redis 地址
- JWT 密钥
- 文件目录 `app.file-storage.root-path`

### 4.4 启动后端

```powershell
cd .\backend
mvn spring-boot:run
```

验证：

- `http://localhost:8080/api/v1/health/check`
- `http://localhost:8080/doc.html`

### 4.5 配置并启动前端

检查 `frontend/.env.development`：

```env
VITE_APP_TITLE=Teacher Internship Platform
VITE_API_BASE_URL=/api
VITE_BACKEND_TARGET=http://localhost:8080
```

启动：

```powershell
cd .\frontend
npm install
npm run dev
```

访问：`http://localhost:5173`

## 5. 打包部署（演示可选）

### 5.1 前端打包

```powershell
cd .\frontend
npm run build
```

### 5.2 后端打包

```powershell
cd .\backend
mvn clean package -DskipTests
java -jar .\target\internship-platform-backend-0.1.0.jar
```

## 6. 答辩展示建议（10-15 分钟）

1. 展示健康检查与接口文档可访问。
2. 使用默认账号完成登录与角色切换。
3. 演示“计划 -> 申请 -> 分配 -> 材料 -> 评价 -> 成绩 -> 消息/统计”闭环。
4. 强调权限隔离与审计追踪能力。

## 7. 常见故障处理

1. `mvn` 未识别：安装 Maven 并配置 `PATH`。
2. 端口冲突：调整 `server.port` 或结束占用进程。
3. 数据库连接失败：检查 `DB_HOST/DB_PORT/DB_USERNAME/DB_PASSWORD`。
4. 文件上传失败：检查 `uploads` 目录存在且有写权限。
5. 登录后 401：检查 JWT 密钥、系统时间、Authorization 头。

