package com.gasen.usercenterbackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "deepseek")
public class DeepSeekConfig {
    private String key;
}
