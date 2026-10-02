package com.ty.controller;

import com.ty.model.AjaxResult;
import com.ty.spring.config.properties.TyProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Data Controller
 *
 * @Author Tommy
 * @Date 2026/10/2
 */
@RestController
@RequestMapping("/data")
@Slf4j
public class DataController {

    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "bmp", "webp"
    );

    private static final int maxSize = 8;

    @Autowired
    private TyProperties props;

    /**
     * 随机返回8张示例图片
     */
    @RequestMapping("/examples")
    public AjaxResult examples() throws Exception {
        Path dir = Paths.get(props.getImageTestRoot()).toAbsolutePath().normalize();
        List<String> imgList = new ArrayList<>();
        if (Files.isDirectory(dir)) {
            try (Stream<Path> stream = Files.walk(dir, 1)) {
                List<Path> pathList = new ArrayList<>(stream
                        .filter(Files::isRegularFile)
                        .filter(DataController::isImageFile)
                        .toList());
                Collections.shuffle(pathList);

                imgList = pathList.stream()
                        .limit(maxSize)
                        .map(DataController::toRelativePath)
                        .collect(Collectors.toList());
            }
        }
        log.info("示例图片数据源：{} {}", props.getImageTestRoot(), Files.isDirectory(dir)? "" : "不存在 或 不是目录");
        return AjaxResult.success(imgList);
    }

    /*
     * 简单判断：是否为图片文件
     */
    private static boolean isImageFile(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        int dot = name.lastIndexOf('.');
        if (dot <= 0 || dot == name.length() - 1) {
            return false;
        }
        return IMAGE_EXTENSIONS.contains(name.substring(dot + 1));
    }

    /*
     * 图片路径转为相对路径
     */
    private static String toRelativePath(Path path) {
        Path parent = path.getParent();
        Path fileName = path.getFileName();

        if (parent == null || fileName == null) {
            return path.toString();
        }

        String parentName = parent.getFileName() != null
                ? parent.getFileName().toString()
                : "";

        return "/" + parentName + "/" + fileName;
    }
}
