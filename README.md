# 图书馆管理系统（网页版）

前后端分离的图书馆管理系统，后端 Spring Boot + JDBC Template，前端 Vue 3 单页应用，SQLite 数据库。

## 一、项目结构

```
library_system_web/
├── pom.xml                             # Maven 项目配置（依赖管理）
├── run.bat                             # 一键编译启动脚本
├── data/                               # 数据库文件目录
│   └── library.db                      # SQLite 数据库文件（自动生成）
├── src/main/java/com/library/
│   ├── LibraryApplication.java         # Spring Boot 启动入口
│   ├── model/                          # 数据模型（普通 Java 类，无框架依赖）
│   │   ├── Book.java                   #   图书实体
│   │   ├── Reader.java                 #   读者实体
│   │   ├── BorrowRecord.java           #   借阅记录实体
│   │   └── User.java                   #   用户实体（含角色枚举）
│   ├── service/                        # 业务逻辑层（使用 JdbcTemplate 手写 SQL）
│   │   ├── AuthService.java            #   认证服务：注册、登录、自动建表
│   │   └── LibraryService.java         #   核心服务：图书/读者/借阅 CRUD
│   └── controller/                     # REST API 控制器
│       ├── AuthController.java         #   /api/auth/login, /api/auth/register
│       ├── BookController.java         #   /api/books (GET/POST/PUT/DELETE)
│       ├── ReaderController.java       #   /api/readers (GET/POST/DELETE)
│       └── BorrowController.java       #   /api/borrow, /api/return, /api/records
└── src/main/resources/
    ├── application.properties          # 服务器端口、SQLite 连接配置
    └── static/                         # 前端文件（无需 npm/yarn）
        ├── index.html                  #   主页面（Vue 3 SPA）
        ├── app.js                      #   Vue 业务逻辑（fetch 调用 API）
        └── style.css                   #   全局样式
```

## 二、设计架构

### 分层架构

```
┌────────────────────────────────────┐
│  浏览器 (Vue 3 SPA)                 │  ← 前端界面，fetch() 调用后端 API
├────────────────────────────────────┤
│  Controller 层 (REST API)          │  ← 接收 HTTP 请求，返回 JSON
├────────────────────────────────────┤
│  Service 层 (业务逻辑)              │  ← JdbcTemplate 手写 SQL
├────────────────────────────────────┤
│  Model 层 (数据实体)               │  ← 普通 POJO，无注解
├────────────────────────────────────┤
│  SQLite 数据库 (单文件)             │  ← data/library.db
└────────────────────────────────────┘
```

### 和桌面版的区别

| | 桌面版 (library_system) | 网页版 (library_system_web) |
|------|------|------|
| 启动方式 | 双击 bat → JavaFX 窗口 | 双击 bat → 浏览器打开 localhost:8080 |
| 界面框架 | JavaFX | Vue 3（CDN 加载） |
| 数据库 | TXT 文件 | SQLite（完整 SQL 支持） |
| SQL 方式 | 无 | JdbcTemplate |
| 包管理 | 手动复制 jar | Maven 自动拉取 |

### REST API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 登录 → `{success, username, role}` |
| POST | `/api/auth/register` | 注册 → `{success, message}` |
| GET | `/api/books` | 获取全部图书 |
| GET | `/api/books/search?keyword=` | 按书名/作者搜索 |
| POST | `/api/books` | 添加图书 |
| PUT | `/api/books/{isbn}` | 修改图书 |
| DELETE | `/api/books/{isbn}` | 删除图书 |
| GET | `/api/readers` | 获取全部读者 |
| POST | `/api/readers` | 添加读者 |
| DELETE | `/api/readers/{id}` | 删除读者 |
| POST | `/api/borrow` | 借书 `{isbn, readerId, days}` |
| POST | `/api/return` | 还书 `{isbn, readerId}` |
| GET | `/api/records` | 获取全部借阅记录 |
| GET | `/api/records/overdue` | 获取逾期记录 |
| GET | `/api/records/my/{readerId}` | 获取某读者的借阅记录 |

### 数据库表结构

| 表名 | 字段 |
|------|------|
| `users` | `username` (主键), `password`, `role` |
| `books` | `isbn` (主键), `title`, `author`, `totalCopies`, `availableCopies` |
| `readers` | `id` (主键), `name`, `borrowedCount`, `maxBorrow` |
| `borrow_records` | `id` (自增主键), `bookIsbn`, `readerId`, `borrowDate`, `dueDate`, `returnDate`, `returned` |

表在 `AuthService.init()` 和 `LibraryService.init()` 中自动创建（`CREATE TABLE IF NOT EXISTS`）。

### 角色权限

| 功能 | 管理员 | 学生 |
|------|:-----:|:----:|
| 添加/删除/修改图书 | ✓ | ✕ |
| 搜索/查看全部图书 | ✓ | ✓ |
| 添加/删除读者 | ✓ | ✕ |
| 查看全部读者 | ✓ | ✕ |
| 借书 / 还书（可选择天数） | ✓ | ✓ |
| 查看全部借阅记录 | ✓ | ✕ |
| 查看我的借阅记录 | ✕ | ✓ |
| 逾期记录查询 | ✓ | ✕ |

## 三、操作手册

### 环境要求

| 软件 | 说明 |
|------|------|
| JDK 25 | 安装在 `D:\jdk` |
| Maven 3.9 | 安装在 `D:\apache-maven-3.9.16`（或任意版本） |
| 浏览器 | Chrome / Edge / Firefox 均可 |

### 首次使用

1. 确保 `data/` 目录存在（项目已包含 `.gitkeep` 占位文件）
2. 双击 `run.bat` 启动（首次会自动下载 Maven 依赖，约 2-5 分钟）
3. 浏览器打开 **http://localhost:8080**

### 日常使用

双击 `run.bat`，看到 `Started LibraryApplication` 后打开浏览器。

### 登录与注册

| 角色 | 默认账号 | 密码 |
|------|----------|------|
| 管理员 | `admin` | `admin123` |
| 学生 | 需自行注册 | 注册时设定 |

- **登录**：输入用户名和密码，点击「登录」
- **注册**：点击「没有账号？去注册」→ 选择角色（学生/管理员）→ 填写信息 → 点击「注册」
- 学生注册后**自动创建对应读者记录**（借书上限 5 本）

### 管理员界面

三个标签页：

| 标签页 | 操作 |
|--------|------|
| **图书管理** | 添加/修改/删除图书，搜索，表格选中回填表单 |
| **读者管理** | 添加/删除读者，查看已借/上限 |
| **借阅管理** | 借书（选天数 7/14/30/60）、还书，逾期筛选，全量记录 |

### 学生界面

两个标签页：

| 标签页 | 操作 |
|--------|------|
| **图书查询** | 搜索/浏览图书，借书（选天数），还书。双击表格可快速填入书号 |
| **我的借阅** | 查看自己的借阅记录，未归还筛选 |

### 查看数据库

浏览器打开 `http://localhost:8080/h2`（H2 控制台），或直接用命令行连接：

```bash
sqlite3 data/library.db ".tables"
sqlite3 data/library.db "SELECT * FROM books;"
```

## 四、技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | JDK 25 | 编译运行 |
| Spring Boot | 3.4.1 | Web 框架 + JDBC |
| SQLite | 3.x (sqlite-jdbc 3.47.1.0) | 数据库 |
| Maven | 3.9.16 | 依赖管理 + 构建 |
| Vue 3 | 3.5.13 (CDN) | 前端框架 |
| Tomcat | 10.1 (内嵌) | Web 服务器 |

## 五、打包为独立 JAR

在项目目录执行：

```bash
D:\apache-maven-3.9.16\bin\mvn clean package -DskipTests -f D:\javawork\library_system_web\pom.xml
```

生成 `target/library-system-1.0.0.jar`，可直接运行：

```bash
java --enable-preview -jar D:\javawork\library_system_web\target\library-system-1.0.0.jar
```

## 六、后续改进方向

### 界面优化
- 替换 Vue CDN 为 npm 项目（支持组件化、路由）
- 添加 CSS 框架（如 Element Plus）美化 UI
- 移动端适配

### 功能扩展
- 密码加密（BCrypt）
- 分页查询（图书/记录多了以后）
- 图书分类 / 标签
- 借阅历史查询 + 统计报表
- 邮件通知（逾期提醒）
- 读者信息修改（目前只能添加和删除）

### 部署
- 打包为 Docker 镜像
- 部署到云服务器（阿里云/腾讯云）
- 配置 HTTPS + 域名

### 开发规范
- 添加单元测试（JUnit + MockMvc）
- API 文档（Swagger/OpenAPI）
- 前端 TypeScript 化
- 日志级别配置
