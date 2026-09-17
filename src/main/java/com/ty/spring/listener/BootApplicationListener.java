package com.ty.spring.listener;

import com.ty.service.VectorService;
import com.ty.spring.config.properties.TyProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * SpringBoot应用启动的监听器
 *
 * <p>在应用启动完毕后，做一些必要的检查与初始化
 *
 * @Author Tommy
 * @Date 2026/9/17
 */
@Component
@Slf4j
public class BootApplicationListener implements ApplicationRunner {

    @Autowired
    private TyProperties tyProperties;

    @Autowired
    private VectorService vectorService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        checkImageGallery();
        buildIndexIfEmpty();
    }

    /**
     * 检查图库资源是否存在
     */
    public void checkImageGallery() {
        if (Files.exists(Paths.get(tyProperties.getImageDataFile()))) {
            log.info("图库：{}", tyProperties.getImageRoot());
            log.info("图库数据标签：{}", tyProperties.getImageDataFile());
        } else {
            log.warn("!图库缺失：{}", tyProperties.getImageRoot());
            log.info("请下载图库：{}", "https://paddle-imagenet-models-name.bj.bcebos.com/dygraph/rec/data/drink_dataset_v2.0.tar");
            log.warn("下载完毕后，请解压，复制 gallery 并重命名 到 {}", tyProperties.getImageRoot());
            log.warn("★图库下载完毕后，请再次启动本应用！");
            System.exit(0);
        }
    }

    /**
     * 若索引为空，则构建索引库。
     *
     * <p>是否执行由配置 auto-build-index-on-empty 控制。
     */
    public void buildIndexIfEmpty() throws Exception {
        if (tyProperties.isAutoBuildIndexOnEmpty()) {
            if (vectorService.isEmpty()) {
                log.info("索引库 {} 为空，即将初始化...", tyProperties.getIndexDir());
                vectorService.build();
            }
        } else {
            log.warn("自动索引构建未开启");
        }
    }
}
