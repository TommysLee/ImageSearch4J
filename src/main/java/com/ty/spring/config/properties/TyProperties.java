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
