# 本地依赖打包与快速启动说明

## 1. 已内置依赖

项目已将以下依赖复制到 `tools` 目录，后续无需再从外部目录寻找：

1. `tools/apache-maven-3.9.9`（Maven 3.9.9）
2. `tools/maven-repo`（离线 Maven 仓库缓存）
3. `tools/mysql/mysql-8.0.37-winx64`（MySQL 二进制）
4. `tools/redis`（Redis 二进制）

## 2. 一键启动

在项目根目录执行：

```powershell
.\tools\start-project.ps1
```

或：

```cmd
.\tools\start-project.cmd
```

启动流程：

1. 启动 MySQL / Redis（优先使用已安装 Windows 服务，失败则回退本地二进制）。
2. 启动后端（固定使用 `tools/apache-maven-3.9.9/bin/mvn.cmd` 与 `tools/maven-repo`）。
3. 启动前端（Vite 端口 `5173`）。

## 3. 一键停止

```powershell
.\tools\stop-project.ps1
```

或：

```cmd
.\tools\stop-project.cmd
```

## 4. 环境检查

```powershell
.\tools\check-deps.ps1
```

会显示：

1. Maven / Maven 仓库 / MySQL / Redis 二进制是否存在
2. 3306 / 6379 / 8080 / 5173 端口状态

## 5. 常用访问地址

1. 前端：`http://127.0.0.1:5173`
2. 后端健康检查：`http://127.0.0.1:8080/api/v1/health/check`
3. 后端接口根：`http://127.0.0.1:8080/api`
