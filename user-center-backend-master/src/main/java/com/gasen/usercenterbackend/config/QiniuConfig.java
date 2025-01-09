package com.gasen.usercenterbackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "qiniu")
public class QiniuConfig {
    private String accessKey;
    private String secretKey;
    private String bucket;
}
