# 本地依赖快速启动

本仓库支持在 Windows 上用一套本地依赖快速拉起 MySQL、Redis、后端和前端，适合首次调试和离线演示。

## 1. 适用场景

- 你不想手工安装 MySQL、Redis 和 Maven
- 你希望一键启动整套平台
- 你想在没有外网的环境里完成演示

## 2. 一键启动

在项目根目录执行：

```powershell
.\tools\start-project.ps1
```

这个脚本会：
- 初始化环境变量
- 启动依赖
- 启动后端
- 启动前端

## 3. 仅启动依赖

如果你只想先把数据库和缓存起起来：

```powershell
.\tools\start-deps.ps1
```

## 4. 依赖状态检查

```powershell
.\tools\check-deps.ps1
```

输出会显示：
- Maven 是否可用
- 本地 Maven 仓库是否存在
- MySQL 是否监听 `3306`
- Redis 是否监听 `6379`
- 后端和前端端口是否占用

## 5. 目录说明

`tools/` 目录中和依赖相关的内容包括：

- `apache-maven-3.9.9/`
- `maven-repo/`
- `mysql/`
- `redis/`
- `env.ps1`
- `start-deps.ps1`
- `start-project.ps1`
- `stop-project.ps1`
- `check-deps.ps1`

## 6. 运行时位置

启动脚本会把运行时文件和日志写到系统用户目录下的：

```text
%LOCALAPPDATA%\TeacherInternshipPlatform\
```

仓库根目录保持整洁，方便重复启动和关闭。

## 7. 默认地址

- 前端：`http://127.0.0.1:5173`
- 后端：`http://127.0.0.1:8080`
- 健康检查：`http://127.0.0.1:8080/api/v1/health/check`

## 8. 如果你用自己的环境

只要确保以下条件即可：
- MySQL 8.0 可连接
- Redis 可连接
- `DB_*`、`REDIS_*`、`FILE_STORAGE_ROOT` 等环境变量配置正确
- 后端启动时 `APP_BOOTSTRAP_ENABLED=true`

