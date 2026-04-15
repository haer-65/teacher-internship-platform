# 数据库初始化说明

本文档说明当前项目的数据库初始化方式、脚本顺序和常见检查点。

## 1. 初始化方式

后端会在启动时自动加载：

```text
backend/src/main/resources/db/bootstrap/V*__*.sql
```

对应配置项：

- `APP_BOOTSTRAP_ENABLED=true`
- `APP_BOOTSTRAP_SCRIPT_LOCATION=classpath:/db/bootstrap/V*__*.sql`

也就是说，当前项目的数据库初始化以后端资源目录下的 bootstrap 脚本为准，不需要手工逐个导入旧版脚本。

## 2. 前置条件

- MySQL 8.0
- Redis 6.x 或兼容版本
- Java 17
- Maven 3.9.9 或以上

如果使用仓库自带的 Windows 工具包，`tools/` 下已经包含本地 Maven、MySQL、Redis 和离线仓库。

## 3. 推荐初始化步骤

### 3.1 启动依赖

```powershell
.\tools\start-deps.ps1
```

该脚本会优先启动本机服务，如果服务不存在，则使用 `tools/` 中的本地依赖启动 MySQL 和 Redis。

### 3.2 启动后端

```powershell
cd .\backend
mvn spring-boot:run
```

首次启动时会自动执行 bootstrap 脚本并初始化库表、种子数据和权限数据。

### 3.3 验证结果

- 数据库是否已创建：`teacher_internship_platform`
- 健康检查是否通过：`http://localhost:8080/api/v1/health/check`
- 默认账号是否可登录：见 [默认账号说明](./05-default-accounts.md)

## 4. 脚本版本范围

当前后端资源目录中可用的 bootstrap 脚本已经到 `V20`，按文件名自然排序执行。

可以把它理解为三段：
- `V1-V2`：建库建表与基础种子
- `V3-V9`：日志、业务模块和菜单补全
- `V10-V20`：国际化、权限范围、数据对齐、字段修正和业务补丁

如果你要重建数据库，最稳妥的方式仍然是清空库后直接重启后端，让启动器按顺序执行全部脚本。

## 5. 关键环境变量

### 数据库

- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USERNAME`
- `DB_PASSWORD`

### Redis

- `REDIS_HOST`
- `REDIS_PORT`
- `REDIS_DB`
- `REDIS_PASSWORD`

### 文件存储

- `FILE_STORAGE_ROOT`
- `FILE_MAX_SIZE_MB`

### 初始化开关

- `APP_BOOTSTRAP_ENABLED`
- `APP_BOOTSTRAP_SCRIPT_LOCATION`

## 6. 常见检查点

### 6.1 表是否完整

重点确认以下表存在：

- `sys_user`
- `sys_role`
- `sys_menu`
- `biz_internship_plan`
- `biz_student_application`
- `biz_assignment`
- `biz_material`
- `biz_evaluation`
- `biz_score_sheet`
- `biz_notice`

### 6.2 初始数据是否就绪

至少确认：

- 角色已初始化
- 菜单权限已绑定
- 基础数据已初始化
- 默认账号可登录
- 计划和申请相关菜单能正常显示

### 6.3 Redis 是否可用

Redis 用于缓存、会话相关辅助能力和部分运行时状态。若 Redis 未启动，后端会在相关能力上报错或退化，因此建议在数据库初始化前先保证 Redis 可用。

