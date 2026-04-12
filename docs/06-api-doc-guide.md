# 接口文档访问说明

## 1. 访问前提

1. 后端服务已启动（默认端口 `8080`）。
2. 已可访问健康检查：`http://localhost:8080/api/v1/health/check`。
3. 需要鉴权的接口先通过登录接口获取 JWT。

## 2. 文档入口

1. Knife4j：`http://localhost:8080/doc.html`
2. Swagger UI：`http://localhost:8080/swagger-ui/index.html`
3. OpenAPI JSON：`http://localhost:8080/v3/api-docs`

## 3. 鉴权方式

请求头统一使用：

```text
Authorization: Bearer <token>
```

## 4. 获取 Token 示例

请求：

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "account": "A0001",
  "password": "123456"
}
```

说明：学生传学号，教师和管理员传工号。

响应（示例）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9....",
    "tokenType": "Bearer",
    "expiresIn": 7200,
    "currentRoleCode": "SYS_ADMIN",
    "roles": [],
    "menuTree": [],
    "permissionCodes": []
  }
}
```

## 5. 接口规范速查

1. 路径前缀：`/api/v1/{module}/{action}`
2. 统一响应结构：`{ code, message, data }`
3. 分页参数：`page`、`size`
4. 上传：`multipart/form-data`
5. 时间格式：`yyyy-MM-dd HH:mm:ss`

## 6. 常见调试接口

1. 健康检查：`GET /api/v1/health/check`
2. 当前用户：`GET /api/v1/auth/me`
3. 计划分页：`GET /api/v1/plan/page`
4. 申请分页（学生）：`GET /api/v1/application/student/page`
5. 材料上传：`POST /api/v1/material/student/upload`

## 7. 常见问题

1. 401 Unauthorized：检查 Token 是否过期、Header 是否含 `Bearer ` 前缀。
2. 403 Forbidden：当前角色无权限，先检查角色菜单和权限码。
3. 上传失败：检查 `Content-Type` 是否为 `multipart/form-data`，并确认文件不超过 20MB。
