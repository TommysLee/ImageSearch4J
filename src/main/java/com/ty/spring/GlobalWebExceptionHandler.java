package com.ty.spring;

import com.ty.exception.CustomException;
import com.ty.model.AjaxResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * SpringMVC 全局异常处理
 *
 * @Author Tommy
 * @Date 2022/2/6
 */
@ControllerAdvice
@Slf4j
public class GlobalWebExceptionHandler {

    /**
     * 以友好的方式返回自定义错误消息
     */
    @ExceptionHandler(CustomException.class)
    @ResponseBody
    public AjaxResult handleCustomException(CustomException e) {
        return AjaxResult.info(e.getCode(), SpringContextHolder.getMessage(e.getMessage()));
    }

    /**
     * 兜底型异常处理
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public AjaxResult handleException(Exception e) {
        log.error(e.toString(), e);
        return AjaxResult.error();
    }
}
