package com.ty.test;

import com.ty.ai.service.PipelineService;
import com.ty.constant.Ty;
import com.ty.model.SearchResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Paths;

/**
 * Pipeline 单元测试
 *
 * @Author Tommy
 * @Date 2026/9/19
 */
@SpringBootTest
public class PipelineTest {

    @Autowired
    private PipelineService pipelineService;

    @Test
    void test() throws Exception {
        String path = Paths.get(Ty.USER_HOME, "image_test", "99.jpg").toString();
        File imgFile = new File(path);
        if (!imgFile.exists()) {
            System.out.println("图片文件不存在：" + path);
            return;
        }

        BufferedImage bufferedImage = ImageIO.read(new File(path));
        SearchResult result = pipelineService.process(bufferedImage);
        System.out.println("以图搜图结果：" + path);
        System.out.println(result);
    }
}
