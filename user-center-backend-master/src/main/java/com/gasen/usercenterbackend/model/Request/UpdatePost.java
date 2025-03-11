package com.gasen.usercenterbackend.model.Request;

import com.gasen.usercenterbackend.model.dao.Post;
import lombok.Data;

import java.io.Serializable;

@Data
public class UpdatePost  implements Serializable {
    // 根据 postId 查找帖子，修改内容前需校验传入的 userId 与帖子的 appId 是否一致
    private Long postId;
    private Integer userId;
    private String content;

    public Post toPost() {
        return new Post().setId(postId).setAppId(userId).setContent(content);
    }
}
