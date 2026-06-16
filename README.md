# 驾考一点通 - 驾考服务管理系统

驾考一点通是一个前后端分离的驾考服务管理系统，覆盖驾校管理、招生报名、练车预约、培训签到、考场线路、报考审核、模拟考试、错题本、报表导出和 AI 问答等业务。

## 技术栈

后端：

- Spring Boot 3.2.5
- Spring Security + JWT
- MyBatis-Plus
- MySQL 8
- Redis Cache
- Lombok
- Hutool
- Apache POI

前端：

- Vue 3
- Vite 5
- Element Plus
- Pinia
- Vue Router
- Axios
- ECharts
- Sass
- 高德地图 JS API

## 快速开始

### 1. 准备环境

- JDK 17+
- Maven 3.9+
- Node.js 18+
- MySQL 8.x
- Redis 6+（后端缓存使用）

### 2. 初始化数据库

创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS jiakao_yidiantong DEFAULT CHARACTER SET utf8mb4;
```

导入主脚本：

```bash
mysql -u root -p jiakao_yidiantong < database/jiakao_yidiantong.sql
```

说明：

- `database/jiakao_yidiantong.sql` 是当前项目主数据库脚本。
- 根目录下的 `jiakao_yidiantong.sql`、`biz_exam_route (1).sql`、`biz_exam_venue (1).sql` 是历史或补充 SQL 文件，除非明确需要，一般优先使用 `database/jiakao_yidiantong.sql`。
- `database/驾驶证考试科目一科目四题库_判断题解析全优化版 (1).xlsx` 是题库相关数据文件。

### 3. 启动后端

```bash
cd springboot
mvn spring-boot:run
```

后端默认信息：

- 服务端口：`8080`
- 上下文路径：`/api`
- 启动类：`com.jiakao.ydt.YdtApplication`
- 登录接口示例：`POST http://127.0.0.1:8080/api/auth/login`

### 4. 启动前端

```bash
cd vue
npm install
npm run dev
```

浏览器访问：

```text
http://127.0.0.1:5173
```

Vite 开发代理已将 `/api` 转发到 `http://127.0.0.1:8080`。

## 配置说明

### 后端环境变量

根目录提供了 `.env.example`，可作为本地或部署环境的配置参考。

数据库配置：

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

Redis 配置：

- `REDIS_HOST`，默认 `localhost`
- `REDIS_PORT`，默认 `6379`
- `REDIS_DATABASE`，默认 `0`
- `REDIS_PASSWORD`，默认空

AI 对话配置：

- `OPENAI_API_KEY`
- `OPENAI_BASE_URL`，OpenAI 兼容接口地址，例如 `https://api.deepseek.com/v1`
- `OPENAI_CHAT_MODEL`，例如 `deepseek-chat`

第三方集成配置：

- `YDT_INTEGRATION_OFFICIAL_EXAM_SCHEDULE_URL`：官方考试能力公布页面地址
- `YDT_INTEGRATION_AMAP_KEY`：高德 Web 服务 Key
- `YDT_INTEGRATION_AMAP_SECRET`：高德 Web 服务数字签名密钥
- `YDT_INTEGRATION_AUTO_CREATE_TRAINING_ON_APPOINTMENT_COMPLETE`：预约完成后是否自动生成培训记录，默认 `true`

生产环境建议通过环境变量覆盖数据库、JWT、AI 和地图相关密钥，不要直接提交真实密钥。

### 前端环境变量

前端环境文件位于：

- `vue/.env.development`
- `vue/.env.production`

当前使用的变量：

- `VITE_BASE_URL=/api`
- `VITE_AMAP_KEY`：高德地图 JS API Key
- `VITE_AMAP_SECURITY_JS_CODE`：高德安全密钥

## 项目结构

```text
DrivingTest/
├── .env.example                     # 后端环境变量示例
├── README.md                        # 项目说明
├── database/                        # 数据库脚本与题库数据
├── springboot/                      # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/jiakao/ydt/
│       │   ├── common/              # 统一响应、角色、异常
│       │   ├── config/              # 安全、缓存、MyBatis、第三方配置
│       │   ├── controller/          # REST API 控制器
│       │   ├── dto/                 # 请求与响应 DTO/VO
│       │   ├── entity/              # 数据实体
│       │   ├── mapper/              # MyBatis-Plus Mapper
│       │   ├── security/            # JWT 与认证上下文
│       │   ├── service/             # 业务服务
│       │   └── util/                # 工具类
│       └── resources/
│           └── application.yml      # 后端默认配置
└── vue/                             # Vue 3 前端
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── api/                     # 前端 API 封装
        ├── assets/                  # 样式与图片资源
        ├── components/              # 通用组件
        ├── layout/                  # 主布局
        ├── router/                  # 路由与权限守卫
        ├── stores/                  # Pinia 状态
        ├── utils/                   # 工具方法
        └── views/                   # 页面模块
```

## 主要功能

公共页面：

- 首页
- 登录
- 学员注册
- 教练注册
- 驾校注册

工作台模块：

- 数据看板：管理员、驾校
- 驾校管理：管理员
- 账号管理：管理员、驾校
- 题库管理：管理员
- 招生管理：管理员、驾校
- 我的学车：学员
- 我的学员：教练
- 练车预约：管理员、驾校、教练、学员
- 培训签到：管理员、驾校、教练、学员
- 考场与线路：管理员、驾校、教练、学员
- 考场报考：管理员、驾校、教练、学员
- 模拟考试：学员
- 考试记录：学员、教练
- 错题本：学员
- 报表导出：管理员、驾校

AI 对话：

- 前端入口在首页或业务页面中调用 AI 对话能力。
- 后端接口：`POST /api/ai/chat`
- 密钥只在后端配置，前端不保存 AI 服务密钥。

地图与官方信息：

- 高德地图用于考场、线路、地理编码和路线规划相关能力。
- 官方考试能力页面地址由 `ydt.integration.official-exam-schedule-url` 配置。

## 测试账号

初始化数据中包含以下账号：

| 角色 | 用户名 | 密码 |
| --- | --- | --- |
| 管理员 | `admin` | `123456` |
| 驾校 | `school1` | `123456` |
| 教练 | `coach1` | `123456` |
| 学员 | `student1` | `123456` |

学员也可以在登录页进入“学员注册”，选择驾校后自助注册。

## 常用命令

后端启动：

```bash
cd springboot
mvn spring-boot:run
```

后端测试：

```bash
cd springboot
mvn test
```

前端开发：

```bash
cd vue
npm run dev
```

前端构建：

```bash
cd vue
npm run build
```

前端预览：

```bash
cd vue
npm run preview
```

## 接口约定

统一响应格式：

```json
{
  "code": 0,
  "message": "成功",
  "data": {}
}
```

登录后请求需要携带：

```text
Authorization: Bearer <token>
```

## 常见问题

### 数据库连接失败

检查 MySQL 是否启动、数据库是否已创建、账号密码是否正确，并确认环境变量或 `springboot/src/main/resources/application.yml` 中的连接信息一致。

### Redis 连接失败

后端启用了 Redis Cache。请确认 Redis 已启动，或通过 `REDIS_HOST`、`REDIS_PORT`、`REDIS_PASSWORD` 调整连接配置。

### 前端接口请求 401/403

确认已经登录，并且请求头中携带了 `Authorization: Bearer <token>`。如果角色无权限访问某页面，前端路由守卫会自动跳转到该角色默认页面。

### AI 提示未配置

确认后端进程已读取 `OPENAI_API_KEY`、`OPENAI_BASE_URL` 和 `OPENAI_CHAT_MODEL`，修改后需要重启后端。

### 地图或路线规划不可用

检查前端 `VITE_AMAP_KEY`、`VITE_AMAP_SECURITY_JS_CODE`，以及后端 `YDT_INTEGRATION_AMAP_KEY`、`YDT_INTEGRATION_AMAP_SECRET` 是否配置正确。

### IDEA 环境变量不生效

在运行配置中打开 **Edit Configurations...**，找到后端启动项的 **Environment variables**，添加环境变量后重新启动后端。
