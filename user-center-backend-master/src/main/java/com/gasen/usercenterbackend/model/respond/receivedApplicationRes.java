package com.gasen.usercenterbackend.model.respond;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class receivedApplicationRes {
    private Integer id;
    private String avatar;
    private String name;
    private Integer state;
}
