package com.ty.constant.enums;

/**
 * Ajax结果类型
 *
 * @Author Tommy
 * @Date 2022/1/26
 */
public enum AjaxResultType {

    /** 成功 **/
    SUCCESS(200, "Success"),

    /** 系统异常 **/
    ERROR(500, "数据处理异常"),

    /** 警告 **/
    WARN(400, "操作错误"),

    /** 登录超时 **/
    TIMEOUT(401, "登录超时，请重新登录！"),

    /** 无权限 **/
    NO_PERMISSION(403, "您无权进行此操作！"),

    /** 未知系统错误 **/
    UNKNOWN(999, "未知错误");

    private final int code;
    private final String message;

    AjaxResultType(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int code() {
        return code;
    }

    public String message() {
        return message;
    }
}
