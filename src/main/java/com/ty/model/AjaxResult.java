package com.ty.model;

import com.ty.constant.enums.AjaxResultType;
import lombok.Data;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 统一Ajax响应的数据结构
 * <p>
 *     为了解决不同的开发人员使用不同的结构来响应给前端，导致规范不统一，开发混乱的问题。我们使用如下代码定义统一数据响应结构：
 * </p>
 *
 * <ul>
 *     <li>state：表示该请求是否处理成功（即是否发生异常）。1表示请求处理成功，0表示处理失败</li>
 *     <li>code：对响应结果进一步细化，200表示请求成功，400表示用户操作导致的异常，500表示系统异常，999表示其他异常。与AjaxResultType枚举一致。</li>
 *     <li>message：友好的提示信息，或者请求结果提示信息。如果请求成功这个信息通常没什么用，如果请求失败，该信息需要展示给用户。</li>
 *     <li>data：通常用于查询数据请求，成功之后将查询数据响应给前端。</li>
 * </ul>
 *
 * @Author Tommy
 * @Date 2022/1/26
 */
@Data
public class AjaxResult {

    private String uuid;
    private int state;
    private int code;
    private String message;
    private Object data;

    /**
     * 新创建 AjaxResult 对象，表示一个空消息
     */
    public AjaxResult() {
    }

    /**
     * 新创建 AjaxResult 对象
     *
     * @param type 结果类型
     */
    public AjaxResult(AjaxResultType type) {
        this(type, type.message(), null);
    }

    /**
     * 新创建 AjaxResult 对象
     *
     * @param type 结果类型
     * @param message 友好的提示消息
     * @param data 数据对象
     */
    public AjaxResult(AjaxResultType type, String message, Object data) {
        this.state = AjaxResultType.SUCCESS == type? 1 : 0;
        this.code = type.code();
        this.message = message;
        this.data = data;
    }

    /**
     * 返回成功消息
     *
     * @return 成功消息
     */
    public static AjaxResult success() {
        return AjaxResult.success(AjaxResultType.SUCCESS.message());
    }

    /**
     * 返回成功消息
     *
     * @param message 友好的提示消息
     * @return 成功消息
     */
    public static AjaxResult success(String message) {
        return AjaxResult.success(message, null);
    }

    /**
     * 返回成功数据
     *
     * @return 成功消息
     */
    public static AjaxResult success(Object data) {
        return AjaxResult.success(AjaxResultType.SUCCESS.message(), data);
    }

    /**
     * 返回成功消息
     *
     * @param message 友好的提示消息
     * @param data 数据对象
     * @return 成功消息
     */
    public static AjaxResult success(String message, Object data) {
        return new AjaxResult(AjaxResultType.SUCCESS, message, data);
    }

    /**
     * 返回警告消息
     *
     * @return 警告消息
     */
    public static AjaxResult warn() {
        return AjaxResult.warn(AjaxResultType.WARN.message());
    }

    /**
     * 返回警告消息
     *
     * @param message 友好的提示消息
     * @return 警告消息
     */
    public static AjaxResult warn(String message) {
        return AjaxResult.warn(message, null);
    }

    /**
     * 返回警告消息
     *
     * @param message 友好的提示消息
     * @param data 数据对象
     * @return 警告消息
     */
    public static AjaxResult warn(String message, Object data) {
        return new AjaxResult(AjaxResultType.WARN, message, data);
    }

    /**
     * 返回错误消息
     *
     * @return 错误消息
     */
    public static AjaxResult error() {
        return AjaxResult.error(AjaxResultType.ERROR.message());
    }

    /**
     * 返回错误消息
     *
     * @param message 友好的提示消息
     * @return 错误消息
     */
    public static AjaxResult error(String message) {
        return AjaxResult.error(message, null);
    }

    /**
     * 返回错误消息
     *
     * @param exception 异常对象
     * @return 错误消息
     */
    public static AjaxResult error(Exception exception) {
        return AjaxResult.error(null, exception);
    }

    /**
     * 返回错误消息
     *
     * @param message 友好的提示消息
     * @param exception 异常对象
     * @return 错误消息
     */
    public static AjaxResult error(String message, Exception exception) {
        if (null != exception) {
            final StringWriter sw = new StringWriter();
            exception.printStackTrace(new PrintWriter(sw));
            message = null == message? "" : message;
            message += "\r\n\tat" + sw.toString();
        }
        return new AjaxResult(AjaxResultType.ERROR, message, null);
    }

    /**
     * 返回自定义消息
     *
     * @param code      Code
     * @param message   友好的提示消息
     * @return 自定义消息
     */
    public static AjaxResult info(int code, String message) {
        return AjaxResult.info(code, message, null);
    }

    /**
     * 返回自定义消息
     *
     * @param code      Code
     * @param message   友好的提示消息
     * @param data      数据对象
     * @return 自定义消息
     */
    public static AjaxResult info(int code, String message, Object data) {
        AjaxResult result = new AjaxResult();
        result.setState(0);
        result.setCode(code);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    /**
     * 设置此数据的UUID
     *
     * @param uuid  UUID
     * @return AjaxResult
     */
    public AjaxResult uuid(String uuid) {
        this.uuid = uuid;
        return this;
    }
}
