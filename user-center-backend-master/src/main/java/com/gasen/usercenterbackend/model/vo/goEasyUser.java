package com.gasen.usercenterbackend.model.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class goEasyUser implements Serializable {
    private Long userId;

    private String userName;

    private String avatar;
}
