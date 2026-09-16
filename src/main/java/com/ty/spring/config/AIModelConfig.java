package com.ty.spring.config;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.onnxruntime.engine.OrtEngine;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import com.ty.ai.translator.GeneralPPLCNetTranslator;
import com.ty.ai.translator.PicodetLcnetMainbodyTranslator;
import com.ty.spring.config.properties.TyProperties;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.nio.file.Paths;

/**
 * AI模型配置类
 *
 * @Author Tommy
 * @Date 2026/9/15
 */
@Configuration
@DependsOn("resourceCopier")   // 保证模型文件先复制完成
@Slf4j
public class AIModelConfig {

    @Autowired
    private TyProperties tyProperties;

    @PostConstruct
    public void init() {
        /*
         * 以下操作解决此问题：ONNX Runtime 每运行一次，都会将 onnxruntime.dll onnxruntime4j_jni.dll onnxruntime_providers_shared.dll 解压到一个临时目录
         * OnnxRuntime 源码中，finally 执行了 cleanUp （通过 File.deleteOnExit 实现），但它只能删除非空目录。因此，会造成大量垃圾数据。
         */

        // 设置ONNX引擎路径（可选，但建议设置）
        if (StringUtils.isNotBlank(tyProperties.getOnnxEnginePath())) {
            System.setProperty("onnxruntime.native.path", tyProperties.getOnnxEnginePath());
            log.info("ONNX引擎路径: {}", tyProperties.getOnnxEnginePath());
        }
    }

    /**
     * 主体检测模型
     */
    @Bean
    public ZooModel<Image, DetectedObjects> mainbodyModel() throws Exception {
        // 1. 使用 Criteria 构建模型加载器
        Criteria<Image, DetectedObjects> criteria = Criteria.builder()
                .optEngine(OrtEngine.ENGINE_NAME)
                .optModelPath(Paths.get(tyProperties.getMainBodyModelPath())) // 指定ONNX模型文件路径
                .setTypes(Image.class, DetectedObjects.class)
                .optTranslator(new PicodetLcnetMainbodyTranslator()) // 前置处理与后置处理的Translator
                .optModelName("PicodetLcnetMainbodyModel")
                .optProgress(new ProgressBar())
                .build();

        // 2. 加载模型
        ZooModel<Image,DetectedObjects> model = criteria.loadModel();
        log.info("成功加载 主体检测ONNX 模型: {}", tyProperties.getMainBodyModelPath());
        return model;
    }

    /**
     * 特征向量模型
     */
    @Bean
    public ZooModel<Image,float[]> generalPPLCNetModel() throws Exception {
        // 1. 使用 Criteria 构建模型加载器
        Criteria<Image, float[]> criteria = Criteria.builder()
                .optEngine(OrtEngine.ENGINE_NAME)
                .optModelPath(Paths.get(tyProperties.getGeneralPPLCNetModelPath())) // 指定ONNX模型文件路径
                .setTypes(Image.class, float[].class)
                .optTranslator(new GeneralPPLCNetTranslator()) // 前处理与后处理的Translator
                .optModelName("GeneralPPLCNetV2Model")
                .optProgress(new ProgressBar())
                .build();

        // 2. 加载模型
        ZooModel<Image,float[]> model = criteria.loadModel();
        log.info("成功加载 特征向量ONNX 模型: {}", tyProperties.getGeneralPPLCNetModelPath());
        return model;
    }
}
