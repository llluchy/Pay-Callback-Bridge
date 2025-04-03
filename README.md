# 微信支付回调代理服务 (WeChat Pay Callback Proxy)

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

一个专为本地开发环境设计的微信支付回调代理服务，解决无公网IP时接收微信支付回调的问题。

## 目录

- [项目背景](#项目背景)
- [运行原理](#运行原理)
- [使用方式](#使用方式)
- [接口文档](#接口文档)
- [代码示例](#代码示例)
- [常见问题](#常见问题)

## 项目背景

在微信支付开发过程中，微信服务器需要向商户系统发送支付结果通知，这要求商户系统提供一个公网可访问的HTTPS回调地址。然而，在本地开发环境中，开发者通常无法提供这样的地址，导致无法完整测试支付流程。

传统解决方案如内网穿透工具（如ngrok、花生壳等）存在一些问题：
- 配置复杂，需要额外维护
- 可能存在安全隐患
- 稳定性受限于第三方服务
- 可能需要付费使用

**微信支付回调代理服务** 提供了一种更简单的解决方案，通过一个公网部署的回调代理服务，临时存储微信支付回调请求，让本地开发环境可以主动查询并处理这些回调请求，从而完成完整的支付测试流程。

## 运行原理

1. **接收回调**：微信支付服务器向代理服务发送支付结果通知
2. **存储请求**：代理服务验证并存储回调请求，生成唯一requestId
3. **查询回调**：本地应用定期查询代理服务是否有新的回调请求
4. **处理业务**：本地应用获取回调详情，进行业务处理
5. **设置响应**：本地应用向代理服务提交处理结果和响应内容
6. **响应回调**：代理服务在再次收到相同回调时，返回预设的响应内容

特别说明：
- 回调数据保存24小时，到期自动删除
- 同一订单的重复回调只保留最新的一个
- 遵循微信支付回调重试机制，在第7次收到相同请求时（约1小时4分钟后），如果仍未设置响应内容，将自动返回成功响应
- 微信支付回调重试时间间隔：`15s/15s/30s/3m/10m/20m/30m/30m/30m/60m/3h/3h/3h/6h/6h`

## 使用方式

### 第1步：配置微信支付回调地址

在微信支付商户平台或API调用中，将回调地址设置为：

```
https://callback-proxy.example.com/api/wechat-pay/callback/{appId}
```

其中`{appId}`替换为您的应用标识，用于区分不同应用的回调数据。

### 第2步：查询回调请求

本地应用定期发送请求查询是否有新的回调：

```
GET https://callback-proxy.example.com/api/wechat-pay/pending-callbacks?appId={appId}
```

### 第3步：获取回调详情

```
GET https://callback-proxy.example.com/api/wechat-pay/callback-detail/{requestId}
```

### 第4步：设置响应内容

```
POST https://callback-proxy.example.com/api/wechat-pay/set-response
Content-Type: application/json

{
    "requestId": "your-request-id",
    "responseBody": "{\"code\":\"SUCCESS\",\"message\":\"成功\"}",
    "httpStatus": 200
}
```

## 接口文档

### 1. 查询待处理回调

**请求**：
```
GET /api/wechat-pay/pending-callbacks?appId={appId}
```

**响应**：
```json
{
    "code": 0,
    "message": "success",
    "data": [
        {
            "requestId": "cb4679e2-f142-4d41-8532-a933c5621e3b",
            "outTradeNo": "2023052210241092",
            "receiveTime": "2023-05-22T08:45:12.345+0800",
            "receivedCount": 2
        }
    ]
}
```

### 2. 获取回调详情

**请求**：
```
GET /api/wechat-pay/callback-detail/{requestId}
```

**响应**：
```json
{
    "code": 0,
    "message": "success",
    "data": {
        "requestId": "cb4679e2-f142-4d41-8532-a933c5621e3b",
        "appId": "your-app-id",
        "outTradeNo": "2023052210241092",
        "headers": {
            "Wechatpay-Signature": "...",
            "Wechatpay-Timestamp": "1624539532",
            "Wechatpay-Nonce": "..."
        },
        "body": "{\"id\":\"cb4679e2-f142-4d41-8532-a933c5621e3b\",\"create_time\":\"2023-05-22T08:45:12+08:00\",\"resource_type\":\"encrypt-resource\",\"event_type\":\"TRANSACTION.SUCCESS\",\"resource\":{\"algorithm\":\"AEAD_AES_256_GCM\",\"ciphertext\":\"...\",\"nonce\":\"...\",\"associated_data\":\"...\"}}",
        "receiveTime": "2023-05-22T08:45:12.345+0800",
        "receivedCount": 2,
        "isResponseSet": false,
        "responseBody": null,
        "responseHttpStatus": null
    }
}
```

### 3. 设置响应内容

**请求**：
```
POST /api/wechat-pay/set-response
Content-Type: application/json

{
    "requestId": "cb4679e2-f142-4d41-8532-a933c5621e3b",
    "responseBody": "{\"code\":\"SUCCESS\",\"message\":\"成功\"}",
    "httpStatus": 200
}
```

**响应**：
```json
{
    "code": 0,
    "message": "设置响应成功",
    "data": null
}
```

## 代码示例

### Java（Spring Boot）客户端示例

```java
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class WechatPayCallbackPoller {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String proxyServerUrl = "https://callback-proxy.example.com/api/wechat-pay";
    private final String appId = "your-app-id";

    /**
     * 定时查询待处理的微信支付回调
     * 每5秒执行一次
     */
    @Scheduled(fixedDelay = 5000)
    public void pollWechatPayCallbacks() {
        try {
            // 1. 查询待处理回调
            String url = proxyServerUrl + "/pending-callbacks?appId=" + appId;
            ResponseEntity<CallbackResponse> response = restTemplate.getForEntity(url, CallbackResponse.class);
            
            if (response.getBody() != null && response.getBody().getCode() == 0) {
                List<CallbackInfo> callbacks = response.getBody().getData();
                
                // 2. 遍历并处理每个回调
                for (CallbackInfo callback : callbacks) {
                    processCallback(callback.getRequestId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理单个回调
     */
    private void processCallback(String requestId) {
        try {
            // 1. 获取回调详情
            String detailUrl = proxyServerUrl + "/callback-detail/" + requestId;
            ResponseEntity<CallbackDetailResponse> detailResponse = 
                    restTemplate.getForEntity(detailUrl, CallbackDetailResponse.class);
            
            if (detailResponse.getBody() != null && detailResponse.getBody().getCode() == 0) {
                CallbackDetail detail = detailResponse.getBody().getData();
                
                // 2. 处理回调数据（这里是您的业务逻辑）
                boolean processResult = handleWechatPayNotification(detail);
                
                // 3. 设置响应内容
                if (processResult) {
                    setCallbackResponse(requestId, "{\"code\":\"SUCCESS\"}", 200);
                } else {
                    setCallbackResponse(requestId, "{\"code\":\"FAIL\",\"message\":\"处理失败\"}", 500);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置回调响应内容
     */
    private void setCallbackResponse(String requestId, String responseBody, int httpStatus) {
        String url = proxyServerUrl + "/set-response";
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("requestId", requestId);
        requestBody.put("responseBody", responseBody);
        requestBody.put("httpStatus", httpStatus);
        
        restTemplate.postForEntity(url, requestBody, Object.class);
    }

    /**
     * 处理微信支付通知的业务逻辑
     * 这里应该实现您自己的业务处理
     */
    private boolean handleWechatPayNotification(CallbackDetail detail) {
        // 1. 解析回调数据体
        String body = detail.getBody();
        // 解密数据、验证签名等操作...
        System.out.println("收到微信支付回调，订单号：" + detail.getOutTradeNo());
        
        // 2. 更新订单状态等业务操作
        // 您的业务代码...
        
        return true; // 返回处理结果
    }
}

// 数据模型类
class CallbackResponse {
    private int code;
    private String message;
    private List<CallbackInfo> data;
    
    // getters and setters...
}

class CallbackInfo {
    private String requestId;
    private String outTradeNo;
    private String receiveTime;
    private int receivedCount;
    
    // getters and setters...
}

class CallbackDetailResponse {
    private int code;
    private String message;
    private CallbackDetail data;
    
    // getters and setters...
}

class CallbackDetail {
    private String requestId;
    private String appId;
    private String outTradeNo;
    private Map<String, String> headers;
    private String body;
    private String receiveTime;
    private int receivedCount;
    private boolean isResponseSet;
    private String responseBody;
    private Integer responseHttpStatus;
    
    // getters and setters...
}
```

### 微信支付回调示例数据（已加密）

```json
{
  "id": "cb4679e2-f142-4d41-8532-a933c5621e3b",
  "create_time": "2023-05-22T08:45:12+08:00",
  "resource_type": "encrypt-resource",
  "event_type": "TRANSACTION.SUCCESS",
  "resource": {
    "algorithm": "AEAD_AES_256_GCM",
    "ciphertext": "...加密的支付结果数据...",
    "nonce": "...",
    "associated_data": "..."
  }
}
```

### 解密后的回调数据示例

```json
{
  "mchid": "1230000109",
  "appid": "wxd678efh567hg6787",
  "out_trade_no": "2023052210241092",
  "transaction_id": "4200000123202305221234567890",
  "trade_type": "JSAPI",
  "trade_state": "SUCCESS",
  "trade_state_desc": "支付成功",
  "bank_type": "CMC",
  "success_time": "2023-05-22T08:45:10+08:00",
  "payer": {
    "openid": "oUpF8uMuAJO_M2pxb1Q9zNjWeS6o"
  },
  "amount": {
    "total": 100,
    "payer_total": 100,
    "currency": "CNY",
    "payer_currency": "CNY"
  }
}
```

## 常见问题

### Q1: 微信支付回调数据安全吗？
**A1**: 代理服务仅临时存储回调数据（24小时），不进行任何解密操作。解密操作只在您的本地应用中进行，确保您的API密钥和证书信息安全。

### Q2: 如何处理重复的回调请求？
**A2**: 代理服务会根据订单号识别重复的回调请求，对于同一订单的回调，只会保留最新的一个。您只需处理最新的回调数据即可。

### Q3: 如果我没有及时处理回调会怎样？
**A3**: 微信支付会按照预设的时间间隔重试发送回调通知。代理服务在收到第7次相同请求（约1小时4分钟后）时，如果您仍未设置响应内容，将自动返回成功响应，避免微信支付系统继续重试。

### Q4: 服务是否收费？
**A4**: 请联系服务提供者获取最新的服务条款和收费标准。

### Q5: 如何获取技术支持？
**A5**: 如有问题或需要技术支持，请联系服务提供者。

---

**联系方式**：
- 邮箱：your-email@example.com
- 微信：your-wechat-id
