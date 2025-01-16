package com.gasen.usercenterbackend.model;

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
@TableName("friendsrequest")
public class FriendsRequest {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer appId;
    private Integer friendId;
    private Integer state;
}
