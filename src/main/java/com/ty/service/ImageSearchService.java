package com.ty.service;

import com.ty.ai.service.PipelineService;
import com.ty.model.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.concurrent.CompletableFuture;

/**
 * 以图搜图业务逻辑服务
 *
 * @Author Tommy
 * @Date 2026/9/16
 */
@Service
@Slf4j
public class ImageSearchService {

    @Autowired
    private PipelineService pipelineService;

    /**
     * 图像搜索
     *
     * @param bufferedImage 待检索的图片
     * @return CompletableFuture<SearchResult>
     */
    @Async("aiInferExecutor")
    public CompletableFuture<SearchResult> search(BufferedImage bufferedImage) throws Exception {
        return CompletableFuture.completedFuture(pipelineService.process(bufferedImage));
    }
}
