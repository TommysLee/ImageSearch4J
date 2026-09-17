package com.ty.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * 图像工具类
 *
 * @Author Tommy
 * @Date 2026/9/17
 */
public class ImageUtils {

    /**
     * 将 Base64 字符串转换为字节数组。
     *
     * @param imageBase64 图片的 Base64 编码字符串
     * @return 图片的原始字节数组
     */
    public static byte[] base64ToBytes(String imageBase64) {
        if (StringUtils.isBlank(imageBase64)) {
            return null;
        }

        // 截取逗号后的纯 Base64 部分
        String pureBase64 = StringUtils.substringAfter(imageBase64, ",");

        // 解码为字节数组
        return Base64.decodeBase64(pureBase64);
    }

    /**
     * 将 Base64 字符串转换为 BufferedImage。
     *
     * @param imageBase64 图片的 Base64 编码字符串
     * @return BufferedImage
     * @throws IOException 图片字节无法解析为有效图片格式时抛出
     */
    public static BufferedImage base64ToBufferedImage(String imageBase64) throws IOException {
        byte[] imageBytes = base64ToBytes(imageBase64);
        return bytesToBufferedImage(imageBytes);
    }

    /**
     * 将 图片字节数组 转换为 BufferedImage
     *
     * @param imageBytes 图片字节数组
     * @return BufferedImage
     * @throws IOException 图片字节无法解析为有效图片格式时抛出
     */
    public static BufferedImage bytesToBufferedImage(byte[] imageBytes) throws IOException {
        if (null == imageBytes) {
            return null;
        }

        BufferedImage bufferedImage;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes)) {
            bufferedImage = ImageIO.read(bis);
        }
        return bufferedImage;
    }
}
