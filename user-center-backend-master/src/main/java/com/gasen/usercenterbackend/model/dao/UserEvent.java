package com.gasen.usercenterbackend.model.dao;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_event")
public class UserEvent implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Integer userId;

    private Long eventId;

    /**
     * 逻辑删除
     */
    @TableLogic(value = "0", delval = "1")
    private Integer isDelete;

    /**
     * 创建账号时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(update = "now()")
    private LocalDateTime updateTime;

    public UserEvent(Integer appId, Long eventId) {
        this.userId = appId;
        this.eventId = eventId;
    }
}
