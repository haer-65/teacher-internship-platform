# 项目目录说明文档

## 1. 根目录结构

```text
teacher-internship-platform/
├─ backend/                              # Spring Boot 后端
├─ frontend/                             # Vue3 前端
├─ sql/                                  # 数据库初始化与增量脚本
├─ docs/                                 # 项目交付文档
├─ uploads/                              # 本地文件存储目录（默认）
├─ temp/                                 # 临时文件目录
├─ README.md
└─ .gitignore
```

## 2. 后端目录说明

```text
backend/
├─ pom.xml
├─ .env.example
└─ src/main/
   ├─ java/com/teacher/internship/
   │  ├─ InternshipPlatformApplication.java
   │  ├─ common/
   │  │  ├─ api/                         # 统一返回结构
   │  │  ├─ entity/                      # 通用实体基类
   │  │  ├─ enums/                       # 通用枚举
   │  │  └─ exception/                   # 全局异常处理
   │  ├─ config/                         # Security/MyBatis/Redis/Swagger/文件存储
   │  ├─ security/                       # JWT 过滤器、权限服务、安全工具
   │  └─ modules/
   │     ├─ health                       # 健康检查
   │     ├─ auth                         # 认证与上下文
   │     ├─ plan                         # 实习计划
   │     ├─ application                  # 申请与分配
   │     ├─ material                     # 材料与版本
   │     ├─ evaluation                   # 指导评价
   │     ├─ score                        # 成绩计算与发布
   │     ├─ notice                       # 站内消息
   │     ├─ stats                        # 统计分析
   │     ├─ system                       # 用户/RBAC/参数/日志/基础数据
   │     └─ base                         # 基础实体与映射
   └─ resources/
      ├─ application.yml
      ├─ application-dev.yml
      └─ application-prod.yml
```

## 3. 前端目录说明

```text
frontend/
├─ package.json
├─ .env.example
├─ .env.development
├─ .env.production
└─ src/
   ├─ main.ts
   ├─ App.vue
   ├─ api/                               # Axios 封装与业务 API
   ├─ constants/                         # 常量定义
   ├─ directives/                        # 权限指令（v-permission）
   ├─ layout/                            # 主框架布局
   ├─ router/                            # 路由与守卫
   ├─ store/                             # Pinia 状态管理
   ├─ styles/                            # 全局样式
   ├─ types/                             # TS 类型定义
   ├─ utils/                             # 工具函数
   └─ views/                             # 各业务页面
      ├─ auth
      ├─ dashboard
      ├─ plan
      ├─ application
      ├─ material
      ├─ evaluation
      ├─ score
      ├─ notice
      ├─ stats
      └─ system
```

## 4. SQL 脚本目录说明

```text
sql/
├─ V1__init_schema.sql
├─ V2__seed_data.sql
├─ V3__seed_plan_data.sql
├─ V4__seed_application_rbac.sql
├─ V5__material_management_ext.sql
├─ V6__evaluation_management_ext.sql
├─ V7__score_management_ext.sql
├─ V8__stats_analysis_ext.sql
└─ V9__menu_component_path_alignment.sql
```

## 5. docs 交付文档说明

1. `03-database-init.md`：数据库初始化步骤与校验。
2. `04-windows-deploy-guide.md`：Windows 本地部署手册。
3. `05-default-accounts.md`：默认账号与权限说明。
4. `06-api-doc-guide.md`：接口文档访问与调试说明。
5. `07-demo-script.md`：答辩演示流程脚本。
6. `08-project-structure.md`：当前文档，目录与职责说明。
7. `09-delivery-checklist.md`：最终交付清单与验收项。
