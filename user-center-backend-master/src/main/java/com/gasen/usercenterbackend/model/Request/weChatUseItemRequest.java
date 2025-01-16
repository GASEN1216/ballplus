package com.gasen.usercenterbackend.model.Request;

import lombok.Data;

@Data
public class weChatUseItemRequest {
    // 用户id
    private Integer userId;

    // url
    private String url;
}
