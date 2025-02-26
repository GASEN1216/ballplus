package com.gasen.usercenterbackend.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class userIdAndAvatar {
    private Integer userId;
    private String avatar;

    public userIdAndAvatar(Integer id, String avatarUrl) {
        this.userId = id;
        this.avatar = avatarUrl;
    }
}
