package com.gasen.usercenterbackend.model.Request;

import lombok.Data;

import java.io.Serializable;

/**
 * 微信添加物品请求
 */
@Data
public class weChatAddItemRequest implements Serializable {
    // 用户id
    private Integer userId;
    // 物品id
    private Integer itemId;
    // url
    private String url;

}
