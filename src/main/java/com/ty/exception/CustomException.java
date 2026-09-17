package com.ty.exception;

import com.ty.constant.enums.AjaxResultType;
import lombok.Getter;

import java.io.Serial;

/**
 * 自定义业务异常，返回友好的提示信息
 * 用途：
 *      前端提交来的数据，不符合业务需要时，通过此异常返回友好的提示信息
 *
 * @Author Tommy
 * @Date 2022/2/6
 */
@Getter
public class CustomException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -8244264121661923630L;

    // 错误代码
    private int code = AjaxResultType.WARN.code();

    /**
     * 自定义异常默认构造函数
     */
    public CustomException() {
    }

    /**
     * 仅包含错误信息的构造函数
     */
    public CustomException(String message) {
        super(message);
    }

    /**
     * 包含错误信息和原始异常的构造函数
     */
    public CustomException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 包含错误码和信息的构造函数（最常用）
     */
    public CustomException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 包含原始异常的构造方法（用于包装受检异常，如 IOException）
     */
    public CustomException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * 包含原始异常的构造方法（用于包装受检异常，如 IOException）
     */
    public CustomException(AjaxResultType type, Throwable cause) {
        super(type.message(), cause);
        this.code = type.code();
    }
}
