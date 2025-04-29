# 球plus 项目

## 概述

一个包含后端服务、微信小程序用户端和管理后台的全栈应用。
旨在提供 [**请在此处根据项目实际情况补充具体业务描述**，例如：球类活动（篮球、足球等）的组织、查找场地、匹配队友、记录成绩等] 功能。

## 结构概述

本项目由以下三个主要部分组成：

*   **后端服务 (`user-center-backend-master`)**: 基于 Java Spring Boot，负责核心业务逻辑、数据持久化和 API 接口提供。
*   **微信小程序 (`miniapp1`)**: 基于原生小程序框架和 TypeScript，提供面向最终用户的移动端界面。
*   **管理后台 (`admin-panel`)**: 基于 Vue 3 和 Element Plus，提供用于管理用户、内容和系统配置的 Web 界面。

微信小程序和管理后台通过调用后端提供的 RESTful API 进行数据交互和业务操作。

## 组成

### 后端服务 (Backend - `user-center-backend-master`)

*   **作用**: 提供核心业务逻辑、用户认证、数据管理及对外 API 接口。
*   **技术栈**: Java 17, Spring Boot 3.2.2, Spring Web, MyBatis Plus, MySQL, Redis, RabbitMQ, Knife4j (API Docs), JWT, Qiniu SDK, Apache HttpClient, FastJSON2, Maven。
*   **环境准备**: JDK 17+, Maven 3.6+, MySQL 服务, Redis 服务, RabbitMQ 服务。
*   **配置**:
    *   主要配置文件位于 `user-center-backend-master/src/main/resources/application.yml`。
    *   **重要**: 首次运行时，请务必修改此文件，配置正确的数据库、Redis、RabbitMQ 连接信息。
    *   **安全警告**: 此配置文件包含敏感密钥 (数据库密码, 微信 appsecret, 七牛云密钥, 高德 Key, DeepSeek Key 等)。**强烈建议**不要将包含明文密钥的配置文件直接提交到代码库。请使用环境变量、外部配置文件或配置中心管理这些敏感信息。
    *   (环境变量使用方法参考 `user-center-backend-master/README.md` 中的示例)。
*   **运行**:
    ```bash
    cd user-center-backend-master
    # 首次运行或依赖变更后，建议先清理和安装依赖
    # mvn clean install
    mvn spring-boot:run
    # 或打包后运行
    # mvn clean package
    # java -jar target/user-center-backend-0.0.1-SNAPSHOT.jar
    ```
*   **API 文档**: 后端启动后，访问 [http://localhost:8080/doc.html](http://localhost:8080/doc.html) 查看 API 详情。

### 微信小程序 (WeChat Mini-program - `miniapp1`)

*   **作用**: 提供用户注册/登录、[**请补充核心功能**，如：浏览活动、报名、查找场地、用户社交互动等] 功能。
*   **技术栈**: 原生微信小程序框架, TypeScript, `miniprogram-recycle-view`。
*   **环境准备**: [微信开发者工具](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html)。
*   **运行**:
    1.  打开微信开发者工具。
    2.  导入项目，选择 `miniapp1` 目录。
    3.  (可选) 在 `project.config.json` 或开发者工具界面中填入您自己的 AppID。
    4.  点击"编译"或"预览"。
    *   **注意**: 小程序需要后端服务正在运行才能正常请求数据。请确保后端服务已启动，并在小程序代码中配置了正确的后端 API 地址（检查 `miniapp1/miniprogram/utils/` 或 `miniapp1/miniprogram/config/` 目录下的相关配置）。

### 管理后台 (Admin Panel - `admin-panel`)

*   **作用**: 提供管理功能，例如：用户管理、[**请补充核心功能**，如：活动管理、内容审核、数据统计等]。
*   **技术栈**: Vue 3, Vite, TypeScript, Pinia, Vue Router, Element Plus, Axios, ECharts, Sass。
*   **环境准备**: Node.js (建议 v18 或更高版本), npm 或 pnpm 或 yarn。
*   **运行**:
    ```bash
    cd admin-panel
    # 安装依赖 (根据您使用的包管理器选择)
    npm install
    # 或者
    # yarn install
    # 或者
    # pnpm install

    # 启动开发服务器
    npm run dev
    ```
    *   **注意**: 管理后台同样需要后端服务正在运行。请确保后端服务已启动，并在管理后台代码中配置了正确的后端 API 地址（检查 `admin-panel/.env` 文件、`admin-panel/src/config/` 或 `admin-panel/src/api/` 等目录下的相关配置）。

## 项目结构

```
/D:/wx-miniprogram/  (项目根目录)
├── user-center-backend-master/  # 后端服务 (Spring Boot)
├── miniapp1/                    # 微信小程序前端
├── admin-panel/                 # 管理后台前端 (Vue 3)
└── README.md                    # 本说明文件
```

## 贡献

*   欢迎为此项目做出贡献！请遵循标准的 Fork & Pull Request 流程。
*   如有 Bug 反馈或功能建议，请提交 Issue。

## 许可证

*   MIT License
