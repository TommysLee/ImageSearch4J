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

    @Autowired
    private VectorService vectorService;

    /**
     * 图像搜索
     *
     * @param bufferedImage 待检索的图片
     * @param topK          返回的最相似图片数量
     * @param scoreThres    置信度阈值‌，低于该值的记录被过滤
     * @return CompletableFuture<SearchResult>
     */
    @Async("aiInferExecutor")
    public CompletableFuture<SearchResult> search(BufferedImage bufferedImage, int topK, float scoreThres) throws Exception {
        // 定位最佳主体
        SearchResult result = pipelineService.process(bufferedImage);

        // 以最佳主体，进行图像向量检索
        if (null != result.getMatch() && null != result.getMatch().getQueryVector()) {
            result.setSimilarList(vectorService.search(result.getMatch().getQueryVector(), topK, scoreThres));
        }
        return CompletableFuture.completedFuture(result);
    }
}
