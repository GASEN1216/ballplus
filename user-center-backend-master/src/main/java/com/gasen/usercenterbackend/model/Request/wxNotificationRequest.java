package com.gasen.usercenterbackend.model.Request;

import com.gasen.usercenterbackend.model.TemplateData;
import lombok.Data;

import java.util.Map;

@Data
public class wxNotificationRequest {
    private String touser;
    private String template_id;
    private String page;
    private String miniprogram_state;
    private Map<String, TemplateData> data;
}
