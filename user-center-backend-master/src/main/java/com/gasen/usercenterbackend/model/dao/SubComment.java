package com.gasen.usercenterbackend.model.dao;

import com.baomidou.mybatisplus.annotation.*;
import com.gasen.usercenterbackend.model.respond.SubCommentDetail;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sub_comment")
public class SubComment {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Integer appId;
    private String appName;
    private String avatar;
    private Integer grade;
    private Long commentId;
    private String content;
    private Integer likes;

    @TableLogic(value = "0", delval = "1")
    private Byte isDelete;

    private LocalDateTime createTime;

    @TableField(update = "now()")
    private LocalDateTime updateTime;

    public SubCommentDetail toSubCommentDetail() {
        return new SubCommentDetail()
                .setAppName(appName)
                .setAvatar(avatar)
                .setGrade(grade)
                .setContent(content)
                .setLikes(likes)
                .setCreateTime(createTime);
    }
}
