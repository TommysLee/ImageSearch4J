package com.ty.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import static com.ty.constant.Ty.DEFAULT_CHARSET;

/**
 * Web工具类
 *
 * @Author Tommy
 * @Date 2022/1/27
 */
@Slf4j
public class WebUtil {

    /** Cookie 默认域名 **/
    private static final String CK_DEFAULT_DOMAIN = "";

    /** Cooike 默认路径 **/
    private static final String CK_DEFAULT_PATH = "/";

    /** Cooike 默认HttpOnly **/
    private static final boolean CK_DEFAULT_HTTP_ONLY = false;

    /**
     * 是否为异步请求
     *
     * @return boolean
     */
    public static boolean isAjax() {
        final String requestType = getHttpRequest().getHeader("X-Requested-With");
        return "XMLHttpRequest".equals(requestType);
    }

    /**
     * 禁用HTTP缓存
     *
     * @param response
     */
    public static void disableHttpCache(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Pragrma", "no-cache");
        response.setDateHeader("Expires", 0);
    }

    /**
     * URL Encode编码字符串
     *
     * @param text
     * @return String
     */
    public static String encodeU8(String text) {
        String result = StringUtils.EMPTY;
        if (StringUtils.isNotBlank(text)) {
            try {
                result = URLEncoder.encode(text, DEFAULT_CHARSET);
            } catch (UnsupportedEncodingException ex) {
                log.error("URL Encode 编码字符串 \" " + text + "\"错误！", ex);
            }
        }
        return result;
    }

    /**
     * URL Decode解码字符串
     *
     * @param encodeText
     * @return String
     */
    public static String decodeU8(String encodeText) {
        String result = StringUtils.EMPTY;
        if (StringUtils.isNotBlank(encodeText)) {
            try {
                result = URLDecoder.decode(encodeText, DEFAULT_CHARSET);
            } catch (UnsupportedEncodingException ex) {
                log.error("URL Encode 解码字符串 \" " + encodeText + "\"错误！", ex);
            }
        }
        return result;
    }

    /**
     * 发送错误状态
     *
     * @param response
     * @param statusCode
     */
    public static void sendError(HttpServletResponse response, int statusCode) throws IOException {
        response.sendError(statusCode);
    }

    /**
     * 发送错误状态
     *
     * @param response
     * @param statusCode
     * @param message
     */
    public static void sendError(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        writeText(response, message);
    }

    /**
     * 重定向到指定URL
     *
     * @param url
     * @param response
     */
    public static void redirect(String url, HttpServletResponse response) {
        if (StringUtils.isNotBlank(url)) {
            try {
                response.sendRedirect(url);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 跳转到指定URL
     *
     * @param url
     * @param request
     */
    public static void forward(String url, HttpServletRequest request, HttpServletResponse response) {
        if (StringUtils.isNotBlank(url)) {
            try {
                request.getRequestDispatcher(url).forward(request, response);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 将字符串写入到输出流
     *
     * @param response
     * @param text
     * @throws IOException
     */
    public static void writeText(HttpServletResponse response, String text) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(text);
    }

    /**
     * 将JSON写入到输出流
     *
     * @param response
     * @param jsonObject
     * @throws IOException
     */
    public static void writeJSON(HttpServletResponse response, Object jsonObject) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(DataUtil.toJSON(jsonObject));
    }

    /**
     * 将JSON对象以JSONP方式写入到response
     *
     * @param response
     * @param funcName
     * @param jsonObject
     * @throws Exception
     */
    public static void writeJSONP(HttpServletResponse response, String funcName, Object jsonObject) throws IOException {
        String text = DataUtil.toJSON(jsonObject);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(funcName + "(" + text + ");");
    }

    /**
     * 获取User Agent
     *
     * @return String
     */
    public static String getUserAgent() {
        return getHttpRequest().getHeader("User-Agent");
    }

    /**
     * 获取HttpServletRequest
     */
    public static HttpServletRequest getHttpRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }

    /**
     * 获取Http Session
     */
    public static HttpSession getHttpSession() {
        return getHttpRequest().getSession();
    }

    /**
     * 获取域名
     *
     * @return String
     */
    public static String getDomain() {
        return getHttpRequest().getServerName();
    }
}
