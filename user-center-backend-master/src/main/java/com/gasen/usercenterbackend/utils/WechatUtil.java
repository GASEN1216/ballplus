package com.gasen.usercenterbackend.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.gasen.usercenterbackend.config.WXConfig;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class WechatUtil {
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
