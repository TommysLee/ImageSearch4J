package com.ty.spring.init;

import com.ty.spring.config.properties.TyProperties;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * 启动初始化：将 classpath 下的 AI 模型（.onnx）复制到指定目录。
 * <p>
 * 兼容 jar / war / IDE 三种运行方式。
 * 约定：所有 .onnx 模型文件直接存放于 classpath 的 {@code models/} 目录下，不含子目录。
 * <p>
 * 通过 {@link PostConstruct} 在 Bean 初始化阶段完成复制，
 * 配合 {@code @DependsOn("resourceCopier")}，保证模型加载类在此之前拿到文件。
 *
 * @Author Tommy
 * @Date 2026/9/15
 */
@Component
@Slf4j
public class ResourceCopier {

    @Autowired
    private TyProperties tyProperties;

    @PostConstruct
    public void init() throws Exception {
        this.extractModels();
    }

    /**
     * 复制AI模型文件（仅复制 .onnx 文件）
     */
    private void extractModels() throws Exception {
        // 如果模型目录不存在，则创建
        Path modelDir = Paths.get(tyProperties.getModelsDir()).toAbsolutePath().normalize();
        Files.createDirectories(modelDir);

        // 使用 Spring 的资源解析器扫描 classpath 下的 models 目录
        final String scanPattern = "classpath*:models/*.onnx";
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources(scanPattern);

        if (resources.length == 0) {
            log.warn("未在 classpath 下找到任何模型文件, pattern={}", scanPattern);
            return;
        }

        // 遍历并复制文件
        int copied = 0;
        int skipped = 0;
        for (Resource resource : resources) {
            // 1. 必须是可读文件
            // 2. 文件名必须以 .onnx 结尾
            String filename = resource.getFilename();
            if (!resource.isReadable() || !"onnx".equalsIgnoreCase(FilenameUtils.getExtension(filename))) {
                continue;
            }

            Path targetPath = modelDir.resolve(filename);
            if (Files.exists(targetPath)) {
                log.info("模型文件已存在，本次跳过：{}", targetPath);
                skipped++;
                continue;
            }

            try (InputStream is = resource.getInputStream()) {
                Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
                log.info("已复制模型文件: {}", targetPath);
                copied++;
            }
        }
        log.info("模型文件复制完成，共复制 {} 个，跳过 {} 个，目标目录：{}", copied, skipped, modelDir);
    }
}
