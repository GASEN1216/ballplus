package com.gasen.usercenterbackend.constant;

public interface LikeConstant {

    int EXPIRE_TIME = 1;

    int POST_TYPE = 0;
    int COMMENT_TYPE = 1;
    int SUB_COMMENT_TYPE = 2;

    String COPY = "copy:";
    String REDIS_POST_LIKES = "ballplus:post:likes:";
    String REDIS_COMMENT_LIKES = "ballplus:comment:likes:";
    String REDIS_SUB_COMMENT_LIKES = "ballplus:subcomment:likes:";
}
