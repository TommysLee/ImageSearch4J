package com.ty.spring.config.properties;

import com.google.common.collect.Maps;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Paths;
import java.util.Map;

import static com.ty.constant.Ty.USER_HOME;

/**
 * 项目属性配置类
 *
 * @Author Tommy
 * @Date 2026/9/14
 */
@ConfigurationProperties(prefix = "ty")
@Data
public class TyProperties {

    /** 视图映射 **/
    private Map<String, String> viewMapping = Maps.newHashMap();

    /** ONNX引擎路径（可选，但建议设置） **/
    private String onnxEnginePath;

    /** AI模型目录 **/
    private String modelsDir = Paths.get(USER_HOME, "models").toString();

    /** 主体检测ONNX模型名称 **/
    private String mainBodyModelName = "picodet_lcnet_x2_5_640_mainbody.onnx";

    /** 特征向量ONNX模型名称 **/
    private String generalPPLCNetModelName = "general_PPLCNetV2.onnx";

    /** Lucene缓冲区大小（单位：MB） **/
    private int luceneBufferSize = 256;

    /** Lucene索引目录 **/
    private String indexDir = Paths.get(USER_HOME, "image_gallery", "vector_index").toString();

    /** 图库根目录 **/
    private String imageRoot = Paths.get(USER_HOME, "image_gallery", "gallery").toString();

    /** 测试图库根目录 **/
    private String imageTestRoot = Paths.get(USER_HOME, "image_gallery", "test_images").toString();

    /** 图库数据标签文件 **/
    private String imageDataFile = Paths.get(imageRoot, "drink_label_all.txt").toString();

    /** 启动时若索引为空，则自动构建 **/
    private boolean autoBuildIndexOnEmpty = true;

    /**
     * 获取主体检测ONNX模型路径
     */
    public String getMainBodyModelPath() {
        return Paths.get(modelsDir, mainBodyModelName).toString();
    }

    /**
     * 获取特征向量ONNX模型路径
     */
    public String getGeneralPPLCNetModelPath() {
        return Paths.get(modelsDir, generalPPLCNetModelName).toString();
    }
}
