# AI每日文章生成系统

基于 Spring Boot + Vue3 + 智谱AI GLM 的每日自动文章生成系统。

## 功能特性

- 支持多个栏目：历史故事、爱情故事、经典寓言、热点新闻、科技热点
- 每个栏目每天自动生成一篇文章
- 集成智谱AI GLM-5模型
- 定时任务自动执行
- **用户认证系统**：登录/注册功能
- **角色权限管理**：管理员和普通用户
- **后台管理系统**：文章增删改查
- 提供REST API接口
- Vue3前端展示页面

## 技术栈

### 后端
- Java 17
- Spring Boot 3.2.x
- Spring Security + JWT
- MyBatis-Plus
- MySQL 8.0
- Hutool

### 前端
- Vue 3
- TypeScript
- Vite
- Element Plus
- Pinia
- Vue Router

## 快速开始

### 1. 数据库初始化

```bash
# 使用MySQL客户端执行SQL脚本
mysql -u root -p123456 < sql/init.sql
mysql -u root -p123456 < sql/user_module.sql
```

或者手动执行 `sql/init.sql` 和 `sql/user_module.sql` 中的SQL语句。

### 2. 配置文件设置

项目使用多环境配置，配置文件位于 `src/main/resources/` 目录：

#### 配置文件说明

| 文件 | 说明 | 是否提交到Git |
|-----|------|--------------|
| `application.yml` | 主配置文件，通用配置 | 是 |
| `application-local.yml` | 本地开发环境配置 | 否（敏感信息） |
| `application-dev.yml` | 开发/测试环境配置 | 否（敏感信息） |
| `application-local.yml.template` | 本地配置模板 | 是 |
| `application-dev.yml.template` | 开发环境配置模板 | 是 |

#### 快速配置步骤

1. **复制配置模板**
   ```bash
   # 进入配置目录
   cd src/main/resources
   
   # 本地开发环境
   cp application-local.yml.template application-local.yml
   
   # 或开发/测试环境
   cp application-dev.yml.template application-dev.yml
   ```

2. **修改配置文件中的敏感信息**
   
   打开 `application-local.yml` 或 `application-dev.yml`，修改以下配置项：
   
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://YOUR_HOST:3306/YOUR_DATABASE  # 数据库地址
       username: YOUR_USERNAME                           # 数据库用户名
       password: YOUR_PASSWORD                           # 数据库密码
     ai:
       zhipu:
         api-key: YOUR_ZHIPU_API_KEY                     # 智谱AI API Key
   
   jwt:
     secret: YOUR_JWT_SECRET_KEY                         # JWT密钥（至少256位）
   
   file:
     upload:
       path: /your/upload/path/                          # 文件上传路径
   ```

3. **选择激活的配置文件**
   
   在 `application.yml` 中设置：
   ```yaml
   spring:
     profiles:
       active: local  # 或 dev
   ```

#### 获取智谱AI API Key

访问 [智谱AI开放平台](https://open.bigmodel.cn/) 注册并获取API Key

### 3. 启动后端

```bash
cd article-generator
mvn spring-boot:run
```

后端服务将在 http://localhost:8080 启动

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端服务将在 http://localhost:3000 启动

## 默认账号

| 角色 | 用户名 | 密码 |
|-----|--------|------|
| 管理员 | admin | admin123 |
| 普通用户 | user | user123 |

## API接口

### 公开接口

| 接口 | 方法 | 说明 |
|-----|------|-----|
| `/api/auth/login` | POST | 用户登录 |
| `/api/auth/register` | POST | 用户注册 |
| `/api/categories` | GET | 获取所有栏目 |
| `/api/articles` | GET | 分页获取文章列表 |
| `/api/articles/{id}` | GET | 获取文章详情 |
| `/api/articles/category/{code}` | GET | 按栏目获取文章列表 |

### 管理员接口 (需要登录 + ADMIN角色)

| 接口 | 方法 | 说明 |
|-----|------|-----|
| `/api/admin/articles` | GET | 获取所有文章（含草稿） |
| `/api/admin/articles` | POST | 新增文章 |
| `/api/admin/articles/{id}` | PUT | 更新文章 |
| `/api/admin/articles/{id}` | DELETE | 删除文章 |
| `/api/admin/articles/generate` | POST | 手动触发所有栏目生成文章 |
| `/api/admin/articles/generate/{categoryId}` | POST | 手动触发指定栏目生成文章 |

## 定时任务

默认配置为每天早上6点自动生成文章，可在 `application.yml` 中修改：

```yaml
schedule:
  cron: "0 0 6 * * ?"  # 每天6点执行
  enabled: true         # 是否启用定时任务
```

## 项目结构

```
article-generator/
├── sql/                          # 数据库脚本
│   ├── init.sql                  # 基础表结构
│   └── user_module.sql           # 用户模块表
├── src/main/java/com/example/article/
│   ├── ArticleApplication.java   # 启动类
│   ├── config/                   # 配置类
│   │   ├── SecurityConfig.java   # Security配置
│   │   ├── JwtProperties.java    # JWT配置
│   │   └── JwtAuthenticationFilter.java
│   ├── controller/               # 控制器
│   │   ├── AuthController.java   # 认证接口
│   │   ├── ArticleController.java
│   │   ├── CategoryController.java
│   │   └── AdminController.java  # 管理接口
│   ├── service/                  # 服务层
│   ├── mapper/                   # MyBatis Mapper
│   ├── entity/                   # 实体类
│   ├── dto/                      # DTO对象
│   ├── util/                     # 工具类
│   │   ├── JwtUtil.java
│   │   └── PasswordUtil.java
│   └── task/                     # 定时任务
├── src/main/resources/
│   └── application.yml           # 配置文件
├── frontend/                     # Vue3前端
│   ├── src/
│   │   ├── views/               # 页面组件
│   │   │   ├── Home.vue
│   │   │   ├── Login.vue
│   │   │   ├── ArticleList.vue
│   │   │   ├── ArticleDetail.vue
│   │   │   └── admin/           # 管理后台
│   │   │       ├── Index.vue
│   │   │       ├── Dashboard.vue
│   │   │       └── Articles.vue
│   │   ├── api/                 # API请求
│   │   ├── stores/              # Pinia状态管理
│   │   │   ├── category.ts
│   │   │   └── user.ts
│   │   └── router/              # 路由配置
│   └── package.json
└── pom.xml
```

## 竞品参考

| 项目 | 借鉴点 |
|-----|-------|
| [ChanCMS](https://github.com/mingkong2023/chancms) | 栏目管理、MySQL架构设计 |
| [The-Daily-Compress](https://github.com/itzCozi/The-Daily-Compress) | 每日自动生成机制 |
| 讯飞绘文 | 多栏目分类、热点追踪 |

## License

MIT
