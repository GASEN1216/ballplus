package com.gasen.usercenterbackend.model.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 投诉记录VO类
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class ComplaintVO {
    /**
     * 投诉ID
     */
    private Long id;
    
    /**
     * 活动ID
     */
    private Long eventId;
    
    /**
     * 投诉人ID
     */
    private Long complainerId;
    
    /**
     * 投诉人名称
     */
    private String complainerName;
    
    /**
     * 投诉人头像
     */
    private String complainerAvatar;
    
    /**
     * 被投诉人ID
     */
    private Long complainedId;
    
    /**
     * 被投诉人名称
     */
    private String complainedName;
    
    /**
     * 被投诉人头像
     */
    private String complainedAvatar;
    
    /**
     * 投诉内容
     */
    private String content;
    
    /**
     * 状态 0-待处理 1-有效 2-无效
     */
    private Byte status;
    
    /**
     * 拒绝原因
     */
    private String rejectReason;
    
    /**
     * 创建时间
     */
    private String createTime;
} 