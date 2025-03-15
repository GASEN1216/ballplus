package com.gasen.usercenterbackend.model.dto;

import lombok.Data;

@Data
public class weChatUseItemRequest {
    // 用户id
    private Integer userId;

    // url
    private String url;
}
