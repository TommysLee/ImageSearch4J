package com.ty.ai.service;

import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.DetectedObjects.DetectedObject;
import com.google.common.collect.Lists;
import com.ty.exception.CustomException;
import com.ty.utils.ImageUtils;
import com.ty.utils.NMSUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * 主体检测服务
 *
 * @Author Tommy
 * @Date 2026/9/16
 */
@Service
@Slf4j
public class MainBodyDetectionService {

    @Autowired
    private GenericObjectPool<Predictor<Image, DetectedObjects>> mainbodyPredictorPool;

    private final double threshold = 0.2;
    private final int maxDetResults = 5;

    /**
     * 对图像进行主体检测
     *
     * @param image 待检测的图片
     * @return List<DetectedObject>
     * @throws Exception
     */
    public List<DetectedObject> predict(Image image) throws Exception {
        List<DetectedObject> resultList = Lists.newArrayList();
        if (null == image) {
            return resultList;
        }

        long begin = System.currentTimeMillis();
        Predictor<Image, DetectedObjects> predictor = null;
        try {
            predictor = mainbodyPredictorPool.borrowObject();
            DetectedObjects results = predictor.predict(image);

            // 根据阈值, 过滤结果
            // 先取前 K 个高分候选，再执行 SANMS，能有效避免大量低分噪声干扰，同时保留高分的“大框吞小框”特性。
            // 形成一个“粗筛保召回、精修保精度”的完美闭环。
            resultList = NMSUtils.sanms(
                    results.topK(maxDetResults) // 取前k个高分候选
                            .stream()
                            .map(r -> (DetectedObjects.DetectedObject) r)
                            .filter(r -> r.getProbability() >= threshold) // 阈值过滤（精修前的最后一道防线）
                            .toList()
            ); // 精修
        } catch (NoSuchElementException e) {
            log.warn("Main Body Predictor pool exhausted", e);
            throw new CustomException("系统繁忙，请稍后重试", e);
        } finally {
            if (predictor != null) {
                mainbodyPredictorPool.returnObject(predictor);
            }
        }
        long end = System.currentTimeMillis();
        log.debug("主体检测-推理耗时：{}ms.", end - begin);
        return resultList;
    }

    /**
     * 对图像进行主体检测
     *
     * @param bufferedImage 待检测的图片
     * @return List<DetectedObject>
     * @throws Exception
     */
    public List<DetectedObject> predict(BufferedImage bufferedImage) throws Exception {
        if (null == bufferedImage) {
            return Lists.newArrayList();
        }
        return this.predict(ImageFactory.getInstance().fromImage(bufferedImage));
    }

    /**
     * 对图像进行主体检测
     *
     * @param imageBase64 待检测图片的Base64格式
     * @return List<DetectedObject>
     * @throws Exception
     */
    public List<DetectedObject> predict(String imageBase64) throws Exception {
        return this.predict(ImageUtils.base64ToBufferedImage(imageBase64));
    }
}
