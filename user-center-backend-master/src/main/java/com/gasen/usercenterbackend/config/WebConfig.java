package com.gasen.usercenterbackend.config;

import com.gasen.usercenterbackend.config.interceptor.GlobalRequestInterceptor;
import com.gasen.usercenterbackend.config.interceptor.LoggingInterceptor;
import com.gasen.usercenterbackend.config.interceptor.WeChatRequestInterceptor;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注册拦截器类
 * @author GASEN
 * @date 2024/3/1 22:50
 * @classType description
 */

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Resource
    private LoggingInterceptor LoggingInterceptor;

    @Resource
    private WeChatRequestInterceptor getWeChatRequestInterceptor;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //日志拦截器
        registry.addInterceptor(LoggingInterceptor)
                .addPathPatterns("/user/**");
        //全局请求拦截器
        registry.addInterceptor(new GlobalRequestInterceptor())
                .addPathPatterns("/user/**")
                .excludePathPatterns("/user/login", "/user/register", "/user/current", "/user/receiveCode", "/user/wx/**"); // 排除不需要检查session的路径
        //微信请求拦截器
        registry.addInterceptor(getWeChatRequestInterceptor)
                .addPathPatterns("/user/wx/**")
                .excludePathPatterns(
                        "/user/wx/login",
                        "/user/wx/token",
                        "/user/wx/uptoken",
                        "/user/wx/update", //update的在方法里进行检测
                        "/user/wx/getEvent",//getEvent避免首页空白
                        "/user/wx/getEventDetailById");//getEventDetailById避免点开通知空白
    }

}
