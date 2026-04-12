# 数据库初始化说明（MySQL 8.0）

## 1. 适用范围

本文档用于本项目在 Windows 本地环境下的数据库首次初始化、种子数据导入、增量脚本执行与结果校验。

数据库名：`teacher_internship_platform`

## 2. 初始化前检查

1. 已安装并启动 MySQL 8.0。
2. 当前用户具备建库建表权限。
3. 客户端字符集建议使用 `utf8mb4`。
4. 确保执行顺序严格按照版本号。

## 3. SQL 脚本执行顺序

| 顺序 | 脚本 | 说明 |
|---|---|---|
| 1 | `V1__init_schema.sql` | 建库、建表、索引、外键 |
| 2 | `V2__seed_data.sql` | 基础字典、角色菜单、权限、默认账号、参数 |
| 3 | `V3__seed_plan_data.sql` | 计划模块演示数据 |
| 4 | `V4__seed_application_rbac.sql` | 申请分配菜单权限补充 |
| 5 | `V5__material_management_ext.sql` | 材料补交控制表与权限补充 |
| 6 | `V6__evaluation_management_ext.sql` | 评价表结构扩展 |
| 7 | `V7__score_management_ext.sql` | 成绩快照字段扩展 |
| 8 | `V8__stats_analysis_ext.sql` | 统计查询索引补充 |
| 9 | `V9__menu_component_path_alignment.sql` | 菜单组件路径对齐 |

## 4. 命令行导入方式（PowerShell）

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

## 5. 导入后校验 SQL

```sql
USE teacher_internship_platform;

-- 表数量
SELECT COUNT(*) AS table_count
FROM information_schema.tables
WHERE table_schema = 'teacher_internship_platform';

-- 默认角色
SELECT role_code, role_name, status
FROM sys_role
WHERE deleted = 0
ORDER BY id;

-- 默认账号
SELECT login_name, real_name, status
FROM sys_user
WHERE deleted = 0
ORDER BY id;

-- 核心业务表示例检查
SELECT COUNT(*) AS plan_count FROM biz_internship_plan WHERE deleted = 0;
SELECT COUNT(*) AS material_type_count FROM biz_material_type WHERE deleted = 0;
```

## 6. 常见问题

1. `Unknown database`：先执行 `V1__init_schema.sql`。
2. 外键报错：检查是否跳脚本或顺序执行错误。
3. 编码乱码：确保连接使用 `utf8mb4`。
4. 重复导入冲突：先清空库后重做，或按脚本内的幂等逻辑执行。

## 7. 课程答辩建议

1. 现场展示时优先展示“已初始化完成”的数据库状态与关键表数量。
2. 说明“脚本版本顺序 + 增量脚本可追溯”是可交付能力。
3. 演示账号提前验证，避免因数据状态导致流程中断。

