package com.ty.controller;

import com.ty.model.AjaxResult;
import com.ty.service.ImageSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.concurrent.CompletableFuture;

/**
 * 以图搜图 Controller
 *
 * @Author Tommy
 * @Date 2026/9/19
 */
@RestController
public class ImageSearchController {

    @Autowired
    private ImageSearchService imageSearchService;

    /**
     * 图像搜索
     */
    @PostMapping("/search")
    public CompletableFuture<AjaxResult> search(@RequestParam("image") MultipartFile file) throws Exception {
        BufferedImage image = ImageIO.read(file.getInputStream());
        return imageSearchService.search(image)
                .thenApply(AjaxResult::success);
    }
}
