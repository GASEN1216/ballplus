package com.gasen.usercenterbackend.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.gasen.usercenterbackend.config.WXConfig;
import com.gasen.usercenterbackend.model.Event;
import com.gasen.usercenterbackend.model.Request.wxNotificationRequest;
import com.gasen.usercenterbackend.model.TemplateData;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.lettuce.core.ScriptOutputType;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.gasen.usercenterbackend.constant.UserConstant.MAX_RETRY_COUNT;
import static com.gasen.usercenterbackend.constant.UserConstant.UNKNOWN_LOCATION;

@Service
@Slf4j
public class WechatUtil {

    private static final String WX_SEND_TEMPLATE_URL = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=";

    private static final String WX_ACCESS_TOKEN_KEY = "wechat:access_token"; // Redis 缓存 key

    private static RedisTemplate<String, Object> redisTemplate;

    private static final RestTemplate restTemplate = new RestTemplate();

    private static WXConfig wxConfig;

    static {
        restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(restTemplate.getRequestFactory()));
    }

    @Resource
    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        WechatUtil.redisTemplate = redisTemplate;
    }

    @Resource
    public void setWxConfig(WXConfig wxConfig) {
        WechatUtil.wxConfig = wxConfig;
    }

    /**
     * 获取 access_token（使用 Redis 缓存）
     */
    private static String getAccessToken() {
        // 先从 Redis 读取 access_token
        String cachedToken = (String) redisTemplate.opsForValue().get(WX_ACCESS_TOKEN_KEY);
        if (cachedToken != null) {
            return cachedToken;
        }

        // 如果 Redis 里没有，就请求微信服务器获取
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + wxConfig.getAppid() + "&secret=" + wxConfig.getAppsecret();
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        // 添加异常处理
        if (response == null || response.containsKey("errcode")) {
            log.error("获取access_token失败：{}", response);
            throw new RuntimeException("微信API调用失败");
        }

        String accessToken = (String) response.get("access_token");
        int expiresIn = (Integer) response.get("expires_in"); // 微信返回的有效时间（秒）

        // 把 access_token 存入 Redis，并设置过期时间（一般微信的有效时间是 7200 秒）
        redisTemplate.opsForValue().set(WX_ACCESS_TOKEN_KEY, accessToken, expiresIn - 200, TimeUnit.SECONDS); // 提前 200 秒刷新

        return accessToken;
    }

    /**
     * 发送活动报名成功通知
     */
    public static boolean sendJoinEventNotification(String openid, Event event) {
        String accessToken = getAccessToken();
        if (accessToken == null) return false;

        String url = WX_SEND_TEMPLATE_URL + accessToken;

        String location = event.getLocation();
        if (location != null && location.length() > 20) {
            location = location.substring(0, 20);
        } else if (location == null || location.isEmpty()) {
            location = UNKNOWN_LOCATION;
        }

        wxNotificationRequest message = new wxNotificationRequest();

        message.setTouser(openid);
        message.setTemplate_id(wxConfig.getSignUpTemplateId());// 报名成功通知的模板ID
        message.setPage("activities/pages/activityDetail/activityDetail?id=" + event.getId());
        message.setMiniprogram_state("developer");

        Map<String, TemplateData> data = new HashMap<>(4);
        data.put("thing2", new TemplateData(event.getName()));  // 活动名称
        data.put("date4", new TemplateData(event.getEventDate().toString()));  // 活动时间
        data.put("thing3", new TemplateData(location));  // 活动场馆
        data.put("phone_number7", new TemplateData(event.getPhoneNumber()));  // 联系电话

        message.setData(data);

        ResponseEntity<String> response = restTemplate.postForEntity(url, message, String.class);

        // 检查请求是否成功
        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("Request failed with status code: {}", response.getStatusCode());
            log.error("Error Response Body: {}", response.getBody());
            return false;
        }

        return true;
    }

    /**
     * 发送活动即将开始通知
     */
    public static boolean sendEventStartNotification(List<String> openidList, Event event) {
        String accessToken = getAccessToken();
        if (accessToken == null) {
            log.error("Failed to send notification: access_token is null");
            return false;
        }

        String url = WX_SEND_TEMPLATE_URL + accessToken;
        List<String> failedOpenids = new ArrayList<>(); // 记录发送失败的 openid
        boolean allSuccess = true; // 是否全部发送成功

        for (String openid : openidList) {
            boolean success = sendSingleNotification(url, openid, event);
            if (!success) {
                failedOpenids.add(openid); // 记录发送失败的 openid
                allSuccess = false;
            }
        }

        // 如果有发送失败的 openid，进行重试
        if (!failedOpenids.isEmpty()) {
            log.warn("Retrying failed notifications for openids: {}", failedOpenids);
            for (int retryCount = 1; retryCount <= MAX_RETRY_COUNT; retryCount++) {
                log.info("Retry attempt {} for failed openids: {}", retryCount, failedOpenids);
                List<String> retryFailedOpenids = new ArrayList<>();

                for (String openid : failedOpenids) {
                    boolean success = sendSingleNotification(url, openid, event);
                    if (!success) {
                        retryFailedOpenids.add(openid); // 记录重试失败的 openid
                    }
                }

                if (retryFailedOpenids.isEmpty()) {
                    log.info("All failed notifications succeeded after retry attempt {}", retryCount);
                    allSuccess = true; // 全部重试成功
                    break;
                }

                failedOpenids = retryFailedOpenids; // 更新失败的 openid 列表
            }

            // 如果重试后仍有失败的 openid，记录错误日志
            if (!failedOpenids.isEmpty()) {
                log.error("Failed to send notifications after {} retries for openids: {}", MAX_RETRY_COUNT, failedOpenids);
                allSuccess = false;
            }
        }

        return allSuccess;
    }

    /**
     * 发送单个通知
     */
    private static boolean sendSingleNotification(String url, String openid, Event event) {
        Map<String, Object> message = new HashMap<>();
        message.put("touser", openid);
        message.put("template_id", wxConfig.getStartReminderTemplateId());
        message.put("page", "activities/pages/activityDetail/activityDetail?eventId=" + event.getId());

        Map<String, Object> data = new HashMap<>();
        data.put("thing4", Map.of("value", event.getName()));  // 活动名称
        data.put("number9", Map.of("value", event.getParticipants()));  // 人数
        data.put("date3", Map.of("value", event.getEventDate().toString()));  // 开始时间
        data.put("thing6", Map.of("value", event.getLocation()));  // 活动地点

        message.put("data", data);

        Map<String, Object> response = restTemplate.postForObject(url, message, Map.class);
        if (response == null) {
            log.error("Failed to send notification to openid: {}, response is null", openid);
            return false;
        }

        if (!response.get("errcode").equals(0)) {
            log.error("Failed to send notification to openid: {}, errcode: {}, errmsg: {}",
                    openid, response.get("errcode"), response.get("errmsg"));
            return false;
        }

        return true;
    }

    public static String getPhoneNumber(String code, String appid, String secret) {
        // Step 1: 获取 Access Token
        String tokenUrl = String.format(
                "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s",
                appid, secret
        );

        Map<String, String> tokenRequestParams = new HashMap<>();
        String tokenResponse = HttpClientUtil.doGet(tokenUrl, tokenRequestParams);
        JSONObject tokenJson = JSON.parseObject(tokenResponse);

        if (!tokenJson.containsKey("access_token")) {
            throw new RuntimeException("Failed to get access_token: " + tokenJson);
        }

        String accessToken = tokenJson.getString("access_token");

        // Step 2: 调用微信手机号获取接口
        String phoneUrl = String.format(
                "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=%s",
                accessToken
        );

        Map<String, String> phoneRequestParams = new HashMap<>();
        phoneRequestParams.put("code", code);

        String phoneResponse = HttpClientUtil.doPostJson(phoneUrl, JSON.toJSONString(phoneRequestParams));
        JSONObject phoneJson = JSON.parseObject(phoneResponse);

        if (!phoneJson.containsKey("phone_info")) {
            throw new RuntimeException("Failed to get phone number: " + phoneJson);
        }

        // Step 3: 返回手机号
        JSONObject phoneInfo = phoneJson.getJSONObject("phone_info");
        return phoneInfo.getString("phoneNumber");
    }

    public static JSONObject getSessionKeyOrOpenId(String code, String appid, String secret) {
        //System.out.println("code: "+ code + " appid: " + appid + " secret: " + secret);

        String requestUrl = "https://api.weixin.qq.com/sns/jscode2session";
        Map<String, String> requestUrlParam = new HashMap<>();
        // https://mp.weixin.qq.com/wxopen/devprofile?action=get_profile&token=164113089&lang=zh_CN
        //小程序appId
        requestUrlParam.put("appid", appid);
        //小程序secret
        requestUrlParam.put("secret", secret);
        //小程序端返回的code
        requestUrlParam.put("js_code", code);
        //默认参数
        requestUrlParam.put("grant_type", "authorization_code");
        //发送post请求读取调用微信接口获取openid用户唯一标识
        String s = HttpClientUtil.doPost(requestUrl, requestUrlParam);
        //System.out.println(s);
        return JSON.parseObject(s);
    }

    // 生成随机用户名的方法
    public static String generateRandomUsername() {
        String prefix = "User_"; // 前缀
        int length = 8; // 随机部分的长度
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        StringBuilder sb = new StringBuilder(prefix);
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }

        return sb.toString();
    }

    // 生成token
    public static String generateToken(String openid, String sessionKey, String secret) {
        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);
        Date expiration = new Date(now + 24 * 60 * 60 * 1000); // 有效期为一天

        Map<String, Object> claims = new HashMap<>();
        claims.put("openid", openid);
        claims.put("sessionKey", sessionKey);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }
}
