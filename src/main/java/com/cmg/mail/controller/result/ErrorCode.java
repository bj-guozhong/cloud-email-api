package com.cmg.mail.controller.result;

public enum ErrorCode {

    SUCCESS(200, true, "SUCCESS"),

    ERROR(500, false, "ERROR"),
    TOKEN_ERROR(99888, false, "ERROR"),

    BAD_REQUEST(400, false, "请求的参数个数或格式不符合要求"),
    INVALID_ARGUMENT(400, false, "请求的参数不正确"),
    UNAUTHORIZED(401, false, "无权访问"),
    FORBIDDEN(403, false, "禁止访问"),
    NOT_FOUND(404, false, "请求的地址不正确"),
    METHOD_NOT_ALLOWED(405, false, "不允许的请求方法"),
    NOT_ACCEPTABLE(406, false, "不接受的请求"),
    CONFLICT(409, false, "资源冲突"),
    UNSUPPORTED_MEDIA_TYPE(415, false, "不支持的Media Type"),
    INTERNAL_ERROR(500, false, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, false, "服务不可用"),
    GATEWAY_TIMEOUT(504, false, "请求服务超时");

    private Integer code;

    private boolean flag;

    private String message;

    ErrorCode(int code, boolean flag, String message) {
        this.code = code;
        this.flag = flag;
        this.message = message;
    }

    public boolean getFlag() {
        return flag;
    }

    public int getCode() {
        return code;
    }


    public String getMessage() {
        return message;
    }
}
