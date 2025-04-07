package com.gasen.usercenterbackend.model.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("friends")
public class Friends {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Long userId;
    private Long friendId;

    public Friends(long userId, long friendId) {
        this.userId = userId;
        this.friendId = friendId;
    }
}
