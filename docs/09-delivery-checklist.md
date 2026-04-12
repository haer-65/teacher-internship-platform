# 最终项目交付清单

## 1. 源码交付

1. 前端源码：`frontend/`
2. 后端源码：`backend/`
3. 数据脚本：`sql/V1` 到 `sql/V9`
4. 交付文档：`docs/03` 到 `docs/09`
5. 说明文档：`README.md`

## 2. 配置与环境交付

1. 前端环境示例：`frontend/.env.example`
2. 后端环境示例：`backend/.env.example`
3. 后端配置：`backend/src/main/resources/application*.yml`
4. 文件存储目录：`uploads/`

## 3. 运行与打包命令

1. 前端开发：`npm run dev`
2. 前端打包：`npm run build`
3. 后端开发：`mvn spring-boot:run`
4. 后端打包：`mvn clean package -DskipTests`
5. 后端运行 Jar：`java -jar .\target\internship-platform-backend-0.1.0.jar`

## 4. 数据初始化检查

1. 已按顺序执行 `V1` -> `V9`。
2. 默认账号可登录。
3. 关键表有基础数据：`sys_user`、`sys_role`、`sys_menu`、`biz_internship_plan`。

## 5. 核心能力验收项

1. 认证鉴权：登录、退出、`/me`、切换角色。
2. RBAC：菜单权限、按钮权限、接口权限生效。
3. 业务闭环：计划 -> 申请 -> 分配 -> 材料 -> 评价 -> 成绩 -> 统计/消息。
4. 文件安全：上传、预览、下载经鉴权接口访问。
5. 审计追踪：操作日志、登录日志、成绩调整记录可查。

## 6. 答辩演示交付包建议

1. 源码目录（含 `sql`、`docs`）。
2. 打包产物（可选）：`frontend/dist`、`backend/target/*.jar`。
3. 演示素材：用于上传的样例文件（pdf/docx/jpg）。
4. 一页纸速查：账号、端口、启动命令、演示路径。
