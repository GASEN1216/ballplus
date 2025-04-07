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
@TableName("friendsrequest")
public class FriendsRequest {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Long appId;
    private Long friendId;
    private Integer state;
}
