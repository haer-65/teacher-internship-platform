# 接口文档说明

平台提供 Swagger / Knife4j 接口文档，便于联调、演示和排查问题。

## 1. 访问地址

- Knife4j：`http://localhost:8080/doc.html`
- Swagger UI：`http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON：`http://localhost:8080/v3/api-docs`
- 健康检查：`http://localhost:8080/api/v1/health/check`

## 2. 认证方式

登录接口成功后会返回 JWT 和当前用户上下文。后续接口请求通常需要在请求头中携带：

```text
Authorization: Bearer <token>
```

如果在 Knife4j 中调试，请先使用登录接口获取 token，再在全局鉴权处填入。

## 3. 统一返回结构

后端接口统一使用：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

出现错误时，`code` 和 `message` 会返回业务或系统错误信息。

## 4. 路径约定

所有业务接口统一以 `/api/v1` 开头，例如：

- `/api/v1/auth/login`
- `/api/v1/plan/page`
- `/api/v1/application/student/page`
- `/api/v1/material/student/page`
- `/api/v1/evaluation/teacher/process/page`
- `/api/v1/score/page`
- `/api/v1/notice/page`
- `/api/v1/stats/overview`

## 5. 常用接口分类

### 5.1 认证

- 登录、退出、当前用户信息
- 角色切换

### 5.2 计划

- 计划分页、详情、创建、更新、发布、结束、归档
- 附件上传、下载、删除
- 材料类型配置

### 5.3 申请

- 学生申请列表
- 申请提交、编辑、撤回
- 管理员审核
- 分配管理和导入

### 5.4 材料

- 学生材料列表与上传
- 教师材料列表
- 管理员材料管理
- 版本历史、预览和下载

### 5.5 评价

- 教师过程评价
- 教师综合评价
- 学生查看评价结果

### 5.6 成绩

- 成绩列表、详情、重算、调整、发布
- 学生成绩单
- 导出 Excel / PDF

### 5.7 通知

- 通知列表、详情、已读、全部已读、删除
- 未读数、最近消息、手动发送、到期提醒触发

### 5.8 统计

- 总览
- 维度列表
- 维度分页
- 过滤选项
- 导出

## 6. 文件上传注意事项

- 接口使用 `multipart/form-data`
- 前端字段名通常为 `file`
- 文件上传目录由 `FILE_STORAGE_ROOT` 控制
- 默认最大单文件大小为 `20MB`

## 7. 调试建议

1. 先调用健康检查，确认后端和数据库已启动。
2. 再登录获取 token。
3. 按“计划 -> 申请 -> 分配 -> 材料 -> 评价 -> 成绩”顺序联调。
4. 如果接口返回权限错误，先检查当前角色和菜单权限。

