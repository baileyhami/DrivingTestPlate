# 驾考一点通 — 驾考服务管理系统

前后端分离：Vue3 + Vite + Element Plus + Pinia + Axios + Vue Router；Spring Boot 3 + MyBatis-Plus + MySQL 8 + JWT + Lombok。

本 README 面向团队成员，包含从零启动、IDEA 环境变量配置、AI 对话使用与常见问题排查。

## 1. 快速开始（本地开发）

1. 初始化数据库：执行 `database/jiakao_yidiantong.sql`。
2. 启动后端：`mvn spring-boot:run`（目录 `springboot`）。
3. 启动前端：`npm run dev`（目录 `vue`）。
4. 浏览器访问：`http://127.0.0.1:5173`。

如果使用 IDE 启动，请先配置环境变量（见第 4 节）。

## 2. 环境依赖

- JDK 17+
- Node 18+
- MySQL 8.x
- Maven 3.9+

## 3. 数据库初始化（详细）

### 3.1 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS jiakao_yidiantong DEFAULT CHARACTER SET utf8mb4;
```

### 3.2 导入脚本

脚本位置：`database/jiakao_yidiantong.sql`

可在命令行执行（示例）：

```bash
mysql -u root -p jiakao_yidiantong < database/jiakao_yidiantong.sql
```

### 3.3 数据库常见问题

- 连接失败：确认 MySQL 版本、端口、账号密码与 `application.yml` 或环境变量一致。
- 表不存在：确认脚本导入成功，且库名为 `jiakao_yidiantong`。

## 4. 后端启动与配置

### 4.1 基本信息

- 目录：`springboot`
- 主类：`com.jiakao.ydt.YdtApplication`
- 服务端口：`8080`
- 上下文路径：`/api`
- 示例登录接口：`POST http://127.0.0.1:8080/api/auth/login`

### 4.2 命令行启动

```bash
cd springboot
mvn spring-boot:run
```

### 4.3 必备环境变量

数据库（必要）：

- `SPRING_DATASOURCE_URL`（示例：`jdbc:mysql://127.0.0.1:3306/jiakao_yidiantong?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&useSSL=false`）
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

AI（可选，启用对话功能时必填）：

- `OPENAI_API_KEY`
- `OPENAI_BASE_URL`（DeepSeek 示例：`https://api.deepseek.com/v1`）
- `OPENAI_CHAT_MODEL`（DeepSeek 示例：`deepseek-chat`）

生产环境请将 `application.yml` 中 `jwt.secret` 替换为足够长的随机密钥，并通过环境变量注入数据库配置。

### 4.4 IDEA 启动配置（推荐）

1. 右上角运行按钮旁边点击配置下拉 → **Edit Configurations...**
2. 选择后端启动配置（`Spring Boot` 或 `Application`）
3. 找到 **Environment variables** → 点击右侧编辑按钮
4. 添加第 4.3 节的环境变量 → **OK** → **Apply**
5. 重新启动后端

### 4.5 AI 对话配置说明

AI 对话功能使用后端转发，**前端不保存密钥**。至少需要配置：

- `OPENAI_API_KEY`（`your_deepseek_api_key`）
- `OPENAI_BASE_URL`（`https://api.deepseek.com/v1`）
- `OPENAI_CHAT_MODEL`（`deepseek-chat`）

如果提示“AI 服务未配置”，请确认后端进程已加载环境变量并重启后端。

## 5. 前端启动与配置

### 5.1 基本信息

- 目录：`vue`
- 开发代理：`vite.config.js` 已将 `/api` 代理到 `http://127.0.0.1:8080`

### 5.2 启动

```bash
cd vue
npm install
npm run dev
```

浏览器访问：`http://127.0.0.1:5173`

### 5.3 生产构建

```bash
cd vue
npm run build
```

将 `dist` 部署到 Nginx 等静态服务器，并反向代理 `/api` 到后端。

## 6. AI 对话功能（前端入口）

- 首页内置 AI 对话模块（登录后可用）。
- 调用后端接口：`POST /api/ai/chat`

## 7. 测试账号（初始化数据）

| 角色   | 用户名   | 密码   |
|--------|----------|--------|
| 管理员 | admin    | 123456 |
| 驾校   | school1  | 123456 |
| 教练   | coach1   | 123456 |
| 学员   | student1 | 123456 |

学员也可在登录页进入「学员注册」，选择驾校后自助注册（角色为学员）。

## 8. 功能与权限摘要

- **管理员**：驾校、题库、考场/线路、全量招生/预约/报考/用户、报表与导出。
- **驾校**：本驾校招生、账号（教练/学员）、预约与培训、报考审核、报表与导出。
- **教练**：本教练预约处理（签到/完成/取消）、培训记录、考场浏览、模拟考试与错题本。
- **学员**：预约练车、培训记录、考场与线路、提交报考、模拟考试、错题本。

## 9. 接口说明

- 统一响应：`{ "code": 0, "message": "成功", "data": ... }`
- 请求头：`Authorization: Bearer <token>`（登录后返回）

## 10. 目录结构

```text
DrivingTest/
├── database/           # SQL 脚本与题库文件
├── springboot/         # Spring Boot 后端
├── vue/                # Vue3 前端
├── .env.example        # 环境变量示例（仅模板）
└── README.md
```

## 11. 常见问题排查

- **AI 提示未配置**：确认后端进程已读到 `OPENAI_API_KEY`，并重启后端。
- **IDEA 环境变量不生效**：必须在 Run Configuration 中设置环境变量，设置后需重启。
- **前端请求 401/403**：确认已登录并携带 `Authorization: Bearer <token>`。
- **接口 500**：查看后端控制台日志定位具体异常。
