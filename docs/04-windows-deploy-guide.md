# Windows 部署指南

本文档面向 Windows 10/11 环境，说明如何在本机快速启动和部署平台。

## 1. 推荐方式：一键启动

项目根目录下已经提供完整的 Windows 启动脚本和本地依赖包。

```powershell
.\tools\start-project.ps1
```

它会自动完成：
- 环境变量初始化
- MySQL 和 Redis 启动
- 后端启动
- 前端启动

对应的停止脚本：

```powershell
.\tools\stop-project.ps1
```

## 2. 依赖准备

### 2.1 本地依赖包

`tools/` 下包含：
- 本地 Maven
- 离线 Maven 仓库
- MySQL
- Redis

如果本机已经安装并运行了自己的 MySQL 和 Redis，也可以直接复用现成服务。

### 2.2 端口

- MySQL：`3306`
- Redis：`6379`
- 后端：`8080`
- 前端：`5173`

## 3. 手动启动流程

### 3.1 启动依赖

```powershell
.\tools\start-deps.ps1
```

### 3.2 启动后端

```powershell
cd .\backend
mvn spring-boot:run
```

### 3.3 启动前端

```powershell
cd .\frontend
npm install
npm run dev
```

## 4. 后端运行说明

后端默认读取 `application.yml` 和 `application-dev.yml`，启动时会使用以下默认值：

- 数据库：`127.0.0.1:3306`
- 数据库名：`teacher_internship_platform`
- Redis：`127.0.0.1:6379`
- 文件存储：`D:/teacher-internship-platform/uploads`

若需改成自定义环境，优先通过环境变量覆盖，而不是直接修改代码。

## 5. 前端运行说明

前端是标准 Vite 项目，开发模式默认访问：

- `http://127.0.0.1:5173`

构建命令：

```powershell
cd .\frontend
npm run build
```

## 6. 生产发布建议

### 6.1 后端打包

```powershell
cd .\backend
mvn clean package -DskipTests
```

然后运行生成的 jar 包。

### 6.2 前端打包

```powershell
cd .\frontend
npm run build
```

将 `dist/` 目录部署到静态资源服务或反向代理后面即可。

## 7. 常见问题

### 7.1 MySQL 或 Redis 没起来

先执行：

```powershell
.\tools\check-deps.ps1
```

再根据结果决定是启动本机服务，还是让 `tools/` 中的本地服务接管。

### 7.2 后端启动后不是空库

如果 `APP_BOOTSTRAP_ENABLED=true`，后端会自动执行 bootstrap 脚本并创建基础数据。首次部署建议保留这个开关开启。

### 7.3 文件上传失败

检查：
- `uploads/` 目录是否存在且可写
- `FILE_STORAGE_ROOT` 是否指向正确路径
- 上传文件是否超过最大限制

