package com.gasen.usercenterbackend.common;

/**
 * 错误码
 * */
public enum ErrorCode {

    SUCCESS(0, "成功", ""),
    PARAMETER_ERROR(1000, "参数错误", ""),
    USER_NOT_EXIST(1001, "用户不存在", ""),
    USER_EXIST(1002, "用户已存在", ""),
    USER_NOT_LOGIN(1003, "用户未登录", ""),
    USER_NOT_LOGIN_OR_NOT_ADMIN(1004, "用户未登录或非管理员", ""),
    SYSTEM_ERROR(1005, "系统错误", ""),
    BANNED_USER(1006, "用户已被封禁", ""),
    SIGNATURE_ERROR(1007, "签名校验失败", ""),
    INVALID_TOKEN(1008, "Token无效或已过期，请重新登录", ""),
    OPERATION_ERROR(1009, "插入数据库操作失败", ""),
    PHONE_NUMBER_ERROR(1010, "获取手机号失败", ""),
    NO_EVENTS(1011, "还没有活动哦，快去创建吧！", ""),
    MATCH_NO_EVENTS(1012, "没有匹配的活动", "");


    private final int code;
    /**
     * 状态码信息
     * */
    private final String message;
    /**
     * 状态码详情
     * */
    private final String detail;

    ErrorCode(int code, String message, String detail) {
        this.code = code;
        this.message = message;
        this.detail = detail;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDetail() {
        return detail;
    }
}
