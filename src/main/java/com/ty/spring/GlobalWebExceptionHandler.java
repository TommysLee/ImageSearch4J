package com.ty.spring;

import com.ty.exception.CustomException;
import com.ty.model.AjaxResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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
     * 处理 Chrome DevTools 自动请求 /.well-known/appspecific/com.chrome.devtools.json 问题
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handleNoResource(NoResourceFoundException ex) throws NoResourceFoundException {
        if (ex.getResourcePath() != null
                && ex.getResourcePath().startsWith(".well-known/")) {
            return ResponseEntity.notFound().build();
        }
        throw ex;
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
