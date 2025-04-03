# WeChat Pay Callback Bridge (WeCB)

<div align="center">
  <img src="docs/images/wecb-logo.png" alt="WeChat Pay Callback Bridge Logo" width="200" />
  <h3>让微信支付回调在本地开发中畅通无阻</h3>
  
  [![GitHub license](https://img.shields.io/github/license/yourusername/wechat-pay-callback-bridge)](https://github.com/yourusername/wechat-pay-callback-bridge/blob/main/LICENSE)
  [![GitHub stars](https://img.shields.io/github/stars/yourusername/wechat-pay-callback-bridge)](https://github.com/yourusername/wechat-pay-callback-bridge/stargazers)
  [![GitHub issues](https://img.shields.io/github/issues/yourusername/wechat-pay-callback-bridge)](https://github.com/yourusername/wechat-pay-callback-bridge/issues)
  [![Maven Central](https://img.shields.io/maven-central/v/com.github.yourusername/wecb-client)](https://search.maven.org/search?q=g:com.github.yourusername%20AND%20a:wecb-client)
</div>

## 📖 简介

**WeChat Pay Callback Bridge** (WeCB) 是一个专为解决微信支付本地开发测试难题而设计的中转服务。它能接收微信支付的回调通知，并将其转发给本地开发环境，让您在没有公网IP的情况下也能完整测试微信支付流程。

### 主要特性

- 🔄 接收微信支付回调并转发给本地环境
- 🕒 支持回调请求的暂存和超时处理
- 🔍 提供Web界面实时查看回调状态
- 🛡️ 内置安全认证机制保护您的数据
- 🌐 支持多开发者、多项目共享同一服务
- 📊 详细的回调历史和统计分析

## 🚀 快速开始

### 服务端部署

1. 下载最新版本
   ```bash
   git clone https://github.com/yourusername/wechat-pay-callback-bridge.git
   cd wechat-pay-callback-bridge
   ```

2. 编译项目
   ```bash
   ./mvnw clean package -DskipTests
   ```

3. 启动服务
   ```bash
   java -jar wecb-server/target/wecb-server-1.0.0.jar
   ```

4. 访问管理界面
   ```
   http://localhost:8080/admin
   初始用户名: admin
   初始密码: admin
   ```

### 客户端集成

1. 添加依赖
   ```xml
   <dependency>
       <groupId>com.github.yourusername</groupId>
       <artifactId>wecb-client</artifactId>
       <version>1.0.0</version>
   </dependency>
   ```

2. 添加配置
   ```yaml
   wecb:
     client:
       server-url: https://your-server.com/api/v1
       api-key: your-api-key
       merchant-id: your-merchant-id
   ```

3. 启用客户端
   ```java
   @SpringBootApplication
   @EnableWeCBClient
   public class MyApplication {
       public static void main(String[] args) {
           SpringApplication.run(MyApplication.class, args);
       }
   }
   ```

## 📊 工作原理

![工作原理图](docs/images/wecb-workflow.png)

1. **配置微信支付回调**：将微信支付回调地址指向WeCB服务器
2. **接收回调**：WeCB服务器接收微信支付的回调请求并临时存储
3. **客户端轮询**：本地开发环境中的WeCB客户端定期检查是否有新回调
4. **处理回调**：WeCB客户端获取回调详情并在本地处理
5. **返回结果**：处理结果通过WeCB服务器返回给微信支付

## 📚 详细文档

访问我们的[完整文档](https://yourusername.github.io/wechat-pay-callback-bridge)，了解详细的：

- [架构设计](https://yourusername.github.io/wechat-pay-callback-bridge/architecture)
- [API参考](https://yourusername.github.io/wechat-pay-callback-bridge/api-reference)
- [高级配置](https://yourusername.github.io/wechat-pay-callback-bridge/advanced-config)
- [安全最佳实践](https://yourusername.github.io/wechat-pay-callback-bridge/security)
- [常见问题](https://yourusername.github.io/wechat-pay-callback-bridge/faq)

## 🌟 使用案例

- **本地开发测试**：无需内网穿透，直接在本地接收微信支付回调
- **CI/CD环境**：在持续集成环境中自动化测试微信支付流程
- **多开发者协作**：团队多个开发者共享同一个回调服务
- **问题复现调试**：通过历史回调记录复现并解决生产问题

## 🤝 参与贡献

我们非常欢迎您的贡献！无论是提交错误报告、改进文档还是提交代码，都将帮助项目变得更好。

1. Fork项目
2. 创建您的特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交您的更改 (`git commit -m 'Add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 创建Pull Request

## 📄 开源许可

本项目基于MIT许可证开源 - 详见 [LICENSE](LICENSE) 文件

## ✨ 致谢

- 感谢所有为本项目做出贡献的开发者
- 感谢微信支付团队提供的优秀支付服务
- 感谢Spring Boot和Vue.js等优秀开源项目

---

<div align="center">
  <sub>Built with ❤️ by 开源社区</sub>
</div>
