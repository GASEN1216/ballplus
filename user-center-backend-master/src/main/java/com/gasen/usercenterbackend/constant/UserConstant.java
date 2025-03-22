package com.gasen.usercenterbackend.constant;

public interface UserConstant {

    String PROJECT_NAME = "ballplus";

    String USER_LOGIN_IN = "userLoginIn";

    int ADMIN = 1;

    int USER = 0;

    int banned = -1;

    int MAX_RETRY_COUNT = 3; // 发送通知最大重试次数

    String UNKNOWN_LOCATION = "未知";

    String REDIS_USER_TOKEN = "ballplus:user:token:";
}
