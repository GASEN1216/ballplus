package com.gasen.usercenterbackend.model.vo;

import com.gasen.usercenterbackend.model.vo.SubCommentDetail;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CommentDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    // 评论ID
    private Long commentId;
    // 所属帖子ID
    private Long postId;
    // 发布评论的用户ID
    private Integer appId;
    // 用户名
    private String appName;
    // 用户头像
    private String avatar;
    // 用户等级
    private Integer grade;
    // 评论内容
    private String content;
    // 点赞数
    private int likes;
    // 创建时间
    private LocalDateTime createTime;
    // 子评论列表
    private List<SubCommentDetail> subComments;
}
