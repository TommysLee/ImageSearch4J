package com.ty.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * MD5 工具类
 *
 * @Author Tommy
 * @Date 2026/9/17
 */
public class MD5Utils {

    /**
     * 计算字节数组的MD5值
     *
     * @param bytes 字节数组
     * @return String
     */
    public static String calc(byte[] bytes) {
        if (null == bytes) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(bytes);
    }

    /**
     * 计算文件的MD5值
     *
     * @param filePath 文件路径
     * @return String
     * @throws IOException
     */
    public static String calc(String filePath) throws IOException {
        if (StringUtils.isBlank(filePath)) {
            return null;
        }

        try (FileInputStream fis = new FileInputStream(filePath)) {
            return calc(fis);
        }
    }

    /**
     * 计算文件的MD5值
     *
     * @param inputStream 文件流对象
     * @return String
     * @throws IOException
     */
    public static String calc(FileInputStream inputStream) throws IOException {
        if (null == inputStream) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(inputStream);
    }
}
