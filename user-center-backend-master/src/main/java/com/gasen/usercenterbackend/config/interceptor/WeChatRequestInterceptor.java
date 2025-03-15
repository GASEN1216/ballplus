package com.gasen.usercenterbackend.config.interceptor;

import com.alibaba.fastjson2.JSONObject;
import com.gasen.usercenterbackend.controller.UserController;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.BufferedReader;

import static com.gasen.usercenterbackend.constant.UserConstant.REDIS_USER_TOKEN;
import static com.gasen.usercenterbackend.constant.UserConstant.USER_LOGIN_IN;

/**
 * 微信请求拦截器
 * @author GASEN
 * @date 2025/1/9 11:00
 * @classType description
 */
@Component
@Slf4j
public class WeChatRequestInterceptor implements HandlerInterceptor {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Bean
    public WeChatRequestInterceptor getWeChatRequestInterceptor() {
        return new WeChatRequestInterceptor();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求中的 token（先从参数中获取）
        String token = request.getHeader("X-Token");

        // 如果参数中没有 token，则尝试从请求体中获取
        if (token == null || token.isEmpty()) {
            token = extractTokenFromBody(request);
        }

        // 如果仍然没有 token，返回 401
        if (token == null || token.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized: Missing Token");
            return false;
        }

        // 验证 token
        boolean isValid = validateToken(token);
        if (!isValid) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized: Invalid Token");
            return false;
        }

        // 如果 token 验证通过，继续处理请求
        return true;
    }

    /**
     * 验证微信小程序登录的token
     * @param token 用户传入的token
     * @return 是否有效的布尔值
     */
    public boolean validateToken(String token) {
        token = REDIS_USER_TOKEN + token;
        // 从Redis中获取token对应的值
        String storedValue = (String) redisTemplate.opsForValue().get(token);

        // 检查token是否存在且有效
        // 如果存在，token有效
        return storedValue != null;
        // 如果Redis中不存在该token，返回无效
    }

    /**
     * 从请求体中提取 token
     * 适用于 Content-Type 为 application/json 的情况
     * @param request HttpServletRequest 请求对象
     * @return token 或 null
     */
    private String extractTokenFromBody(HttpServletRequest request) {
        try {
            StringBuilder body = new StringBuilder();
            String line;
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
            // 确保请求体不为空
            if (body.isEmpty()) {
                log.error("Request body is empty");
                return null;
            }
            // 使用 FastJSON 解析请求体
            JSONObject jsonObject = JSONObject.parseObject(body.toString());
            // 确保解析后的对象不为 null
            if (jsonObject == null) {
                log.error("Failed to parse request body as JSON");
                return null;
            }
            return jsonObject.getString("token");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
