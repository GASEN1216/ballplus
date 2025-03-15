package com.gasen.usercenterbackend.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SubCommentDetail {
    private String appName;
    private String avatar;
    private Integer grade;
    private String content;
    private Integer likes;
    private LocalDateTime createTime;
}
