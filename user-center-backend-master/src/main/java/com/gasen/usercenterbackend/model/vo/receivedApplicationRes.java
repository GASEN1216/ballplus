package com.gasen.usercenterbackend.model.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class receivedApplicationRes {
    private Long id;
    private String avatar;
    private String name;
    private Integer state;
}
