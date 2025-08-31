# Spring Boot 实时日志监控系统## 部署建议

1. **直接部署方式**：将应用直接部署在需要监控日志的服务器上，应用可以直接访问本地日志文件
2. **文件访问权限**：确保运行应用的用户有权限读取需要监控的日志文件
3. **生产环境安全**：在生产环境中，应配置适当的安全措施，如网络访问控制、身份验证等

这是一个基于Spring Boot的实时日志监控系统，提供类似Unix `tail -f`命令的功能，可以直接在服务器上部署并监控本地日志文件。

## 功能特性

1. **WebSocket实时推送**: 通过WebSocket连接实时推送日志更新
2. **Web界面监控**: 提供友好的Web界面用于监控日志文件
3. **RESTful API**: 提供API接口用于获取文件内容
4. **文件下载**: 支持下载完整日志文件

## 技术栈

- Spring Boot 2.7.0
- Spring WebSocket
- HTML/CSS/JavaScript (前端)
- Java 8+

## 项目结构

```
spring-boot-tail-app/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── top/allhere/logMonitoring/
│   │   │       ├── Application.java           # 主应用入口
│   │   │       ├── WebSocketConfig.java       # WebSocket配置
│   │   │       ├── LogTailWebSocketHandler.java # WebSocket处理器
│   │   │       └── LogController.java         # REST控制器
│   │   └── resources/
│   │       ├── application.properties         # 应用配置
│   │       └── static/
│   │           └── index.html                # 前端界面
├── pom.xml                                   # Maven配置文件
└── README.md                                 # 项目说明文件
```

## 安装和运行

### 环境要求

- Java 8 或更高版本
- Maven 3.6 或更高版本

### 构建项目

```bash
cd spring-boot-tail-app
mvn clean package
```

### 运行应用

```bash
mvn spring-boot:run
```

或者

```bash
java -jar target/spring-boot-tail-app-1.0.0.jar
```

应用默认运行在 8080 端口。

## 使用方法

### 通过Web界面使用

1. 将应用部署到目标服务器上
2. 打开浏览器访问 `http://localhost:8080` (或相应的服务器地址)
3. 在输入框中输入要监控的日志文件完整路径
4. 点击"开始监控"按钮开始实时监控
5. 点击"停止监控"按钮停止监控
6. 点击"下载文件"按钮下载完整日志文件

### 通过API使用

#### 获取文件最后N行

```
GET /tail?filePath={文件路径}&lines={行数}
```

示例:
```
GET /tail?filePath=/var/log/app.log&lines=20
```

#### 下载完整文件

```
GET /download?filePath={文件路径}
```

示例:
```
GET /download?filePath=/var/log/app.log
```

## 配置说明

在 [application.properties](file://D:/project/spring-boot-tail-app/src/main/resources/application.properties) 文件中可以配置以下参数:

- `server.port`: 应用运行端口，默认为8080
- `logging.level.*`: 日志级别配置
- `spring.servlet.multipart.max-file-size`: 文件上传最大大小
- `spring.servlet.multipart.max-request-size`: 请求最大大小
- `server.compression.*`: 响应压缩配置

## 安全说明

> 注意：此应用默认没有安全认证机制，任何能够访问应用的人都可以查看服务器上的文件。
> 在生产环境中使用时，请务必添加适当的安全措施，例如：
> 1. 添加身份验证和授权机制
> 2. 限制可访问的文件路径
> 3. 部署在内网环境中
> 4. 使用HTTPS协议

## 扩展功能

您可以基于此项目进一步扩展功能：

1. 添加用户认证和权限控制
2. 支持多个文件同时监控
3. 添加日志搜索和过滤功能
4. 实现日志文件管理（删除、归档等）
5. 添加邮件或短信告警功能
6. 使用密钥认证替代密码认证
7. 添加连接池管理SSH连接