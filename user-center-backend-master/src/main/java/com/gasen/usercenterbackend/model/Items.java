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
@TableName("items")
public class Items {
    private Integer userId;
    private Integer itemId;

    public Items(Integer userId, Integer itemId) {
        this.userId = userId;
        this.itemId = itemId;
    }
}
