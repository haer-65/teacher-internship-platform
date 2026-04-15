# 项目结构说明

本文档说明仓库的目录职责和当前代码组织方式，帮助快速定位功能模块。

## 1. 根目录

```text
teacher-internship-platform/
├─ backend/    # Spring Boot 后端
├─ frontend/   # Vue3 前端
├─ sql/        # 数据库脚本
├─ docs/       # 交付文档
├─ tools/      # Windows 启动与依赖工具
├─ uploads/    # 本地文件存储
├─ README.md
└─ .gitignore
```

## 2. 后端结构

后端主目录：

```text
backend/
├─ pom.xml
└─ src/main/
   ├─ java/com/teacher/internship/
   └─ resources/
```

### 2.1 Java 分层

- `common`：统一返回、异常、枚举、基础实体
- `config`：安全、Redis、MyBatis、Swagger、文件存储、启动器
- `security`：JWT、认证上下文、权限服务、异常处理
- `modules`：各业务模块

### 2.2 业务模块

- `auth`：登录、退出、当前用户、角色切换
- `system`：用户、角色、菜单、参数、日志、基础数据
- `base`：基础字典实体与映射
- `plan`：实习计划、附件、材料类型
- `application`：申请和分配
- `material`：过程材料和版本
- `evaluation`：过程/综合评价
- `score`：成绩计算与发布
- `notice`：站内通知
- `stats`：统计分析

### 2.3 资源目录

- `application.yml`：主配置
- `application-dev.yml`：开发配置
- `application-prod.yml`：生产配置
- `db/bootstrap/`：启动时执行的初始化和补丁脚本
- `db/migration/`：迁移脚本

## 3. 前端结构

前端主目录：

```text
frontend/
├─ package.json
├─ index.html
└─ src/
```

### 3.1 关键目录

- `api/modules`：按业务模块封装接口调用
- `layout`：主布局、侧边栏、顶部栏
- `router`：路由和守卫
- `store`：Pinia 状态管理
- `views`：页面视图
- `components`：可复用组件
- `utils`：token、权限、预览和辅助工具
- `styles`：全局样式

### 3.2 页面模块

- `auth`：登录页
- `dashboard`：工作台
- `plan`：计划管理与学生浏览
- `application`：申请和分配
- `material`：材料管理
- `evaluation`：评价管理
- `score`：成绩管理
- `notice`：通知中心
- `stats`：统计分析
- `system`：系统管理

## 4. SQL 结构

`sql/` 目录保存数据库初始化和增量脚本副本，主要用于离线查看和手工参考。

当前版本的后端实际加载来源是 `backend/src/main/resources/db/bootstrap/`。

## 5. 工具目录

`tools/` 目录保存 Windows 本地化运行所需的脚本和依赖：

- `start-project.ps1`：一键启动
- `stop-project.ps1`：一键停止
- `start-deps.ps1`：启动 MySQL 和 Redis
- `check-deps.ps1`：检查依赖状态
- `env.ps1`：初始化本地环境变量
- 本地 Maven、MySQL、Redis：脱机运行支持

## 6. 构建产物

- `backend/target/`：后端打包输出
- `frontend/dist/`：前端构建输出

这两个目录都是构建产物，不建议手工维护。

