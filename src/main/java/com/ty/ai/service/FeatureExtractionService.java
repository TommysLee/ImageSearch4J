package com.ty.ai.service;

import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.translate.TranslateException;
import com.ty.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.NoSuchElementException;

/**
 * 特征提取服务
 *
 * @Author Tommy
 * @Date 2026/9/16
 */
@Service
@Slf4j
public class FeatureExtractionService {

    @Autowired
    private GenericObjectPool<Predictor<Image, float[]>> generalPPLCNetPredictorPool;

    /**
     * 从图片中提取特征向量
     *
     * @param image 待处理的图片
     * @return float[] 特征向量（512 维，已 L2 归一化）
     * @throws TranslateException
     */
    public float[] predict(Image image) throws Exception {
        if (null == image) {
            return null;
        }

        Predictor<Image, float[]> predictor = null;
        float[] result;
        try {
            predictor = generalPPLCNetPredictorPool.borrowObject();
            result = predictor.predict(image);
        } catch (NoSuchElementException e) {
            log.warn("Feature Extraction Predictor pool exhausted", e);
            throw new CustomException("系统繁忙，请稍后重试", e);
        } finally {
            if (predictor != null) {
                generalPPLCNetPredictorPool.returnObject(predictor);
            }
        }
        return result;
    }

    /**
     * 从图片中提取特征向量
     *
     * @param bufferedImage 待处理的图片
     * @return float[] 特征向量（512 维，已 L2 归一化）
     * @throws Exception
     */
    public float[] predict(BufferedImage bufferedImage) throws Exception {
        if (null == bufferedImage) {
            return null;
        }
        return this.predict(ImageFactory.getInstance().fromImage(bufferedImage));
    }
}
