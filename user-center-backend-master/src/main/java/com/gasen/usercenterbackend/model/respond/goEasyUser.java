package com.gasen.usercenterbackend.model.respond;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class goEasyUser implements Serializable {
    private Integer userId;

    private String userName;

    private String avatar;
}
