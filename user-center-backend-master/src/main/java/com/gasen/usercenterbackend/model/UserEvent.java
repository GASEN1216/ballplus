package com.gasen.usercenterbackend.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
