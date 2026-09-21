package com.ty.ai.service;

import ai.djl.modality.cv.output.DetectedObjects.DetectedObject;
import ai.djl.modality.cv.output.Rectangle;
import com.ty.model.SearchResult;
import com.ty.model.VectorDocument;
import com.ty.service.VectorService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * 百度 PP-Shitu 等效实现的 Pipeline
 *
 * @Author Tommy
 * @Date 2026/9/16
 */
@Service
@Slf4j
public class PipelineService {

    @Autowired
    private MainBodyDetectionService mainBodyDetectionService;

    @Autowired
    private VectorService vectorService;

    /**
     * PP-Shitu Pipeline：主体检测 → 子图特征提取 → 向量检索 → 二次排序取最优。
     *
     * <p>负责"定位最佳主体"——从多个候选中，找到最可能匹配向量库的那个主体。
     *
     * <p>即便向量库中无匹配项，返回结果的候选集依然包含主体检测结果，
     * 前端可据此提示用户"已检测到目标，但向量库中无匹配"。
     *
     * @param bufferedImage 待检索的图片
     * @return 候选集、最佳主体框与匹配文档
     * @throws Exception 主体检测或向量检索失败时抛出
     */
    public SearchResult process(BufferedImage bufferedImage) throws Exception {
        long begin = System.currentTimeMillis();
        SearchResult result = new SearchResult();

        // 1. 主体检测，获取 Top5 候选集
        List<DetectedObject> candidates = mainBodyDetectionService.predict(bufferedImage);
        result.setCandidates(candidates);
        if (CollectionUtils.isEmpty(candidates)) {
            return result;
        }

        // 2. 逐一处理每个候选：裁剪子图 → 特征提取 → 向量检索 → 记录最优
        // "候选 → 各自检索 → 二次排序"的策略：优化并提高召回率。即每个候选独立检索后取全局最优。
        VectorDocument bestMatch = null;
        Rectangle bestBbox = null;
        DetectedObject bestCandidate = null;
        for (DetectedObject c : candidates) {
            Rectangle box = c.getBoundingBox().getBounds();

            // 子图 → 向量检索（取 Top1）
            BufferedImage subImage = bufferedImage.getSubimage((int) box.getX(), (int) box.getY(), (int) box.getWidth(), (int) box.getHeight());
            List<VectorDocument> docs = vectorService.search(subImage, 1, 0.5f);
            if (CollectionUtils.isEmpty(docs)) {
                continue;
            }

            // 记录当前最高分
            // 同等分数，取面积大的，这样更符合以图搜图场景
            VectorDocument doc = docs.get(0);
            if (null == bestMatch || doc.getScore() > bestMatch.getScore()) {
                bestMatch = doc;
                bestBbox = box;
                bestCandidate = c;
            } else if (doc.getScore() == bestMatch.getScore()) {
                if (box.getWidth() * box.getHeight() > bestBbox.getWidth() * bestBbox.getHeight()) {
                    bestMatch = doc;
                    bestBbox = box;
                    bestCandidate = c;
                }
            }
        }

        // 3. 若有匹配，写入结果
        if (null != bestMatch) {
            result.setMatch(bestMatch);
            result.setBbox(bestBbox);
            candidates.remove(bestCandidate); // 匹配结果从候选集中移除
        }

        long end = System.currentTimeMillis();
        if (bestMatch != null) {
            log.info("PP-Shitu Pipeline 定位最佳主体 耗时：{} ms，候选数：{}，匹配结果：{}，匹配分数：{}，目标框：x={} y={} w={} h={}",
                    end - begin, candidates.size() + 1,
                    bestMatch.getName(),
                    bestMatch.getScore(),
                    bestBbox.getX(), bestBbox.getY(), bestBbox.getWidth(), bestBbox.getHeight());
        } else {
            log.info("PP-Shitu Pipeline 定位最佳主体 耗时：{} ms，候选数：{}，无匹配", end - begin, candidates.size());
        }
        return result;
    }
}
