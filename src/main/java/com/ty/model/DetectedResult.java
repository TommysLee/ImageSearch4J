package com.ty.model;

import ai.djl.modality.cv.output.DetectedObjects.DetectedObject;
import lombok.Data;
import org.apache.commons.lang3.ArrayUtils;

import java.io.Serial;
import java.io.Serializable;

/**
 * DJL DetectedObject 简化版，便于 SpringMVC JSON 化
 *
 * @Author Tommy
 * @Date 2026/9/20
 */
@Data
public class DetectedResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1691463041090803418L;

    private String className;
    private Double probability;
    private Double[] rect;

    public DetectedResult(DetectedObject detectedObject) {
        if (null != detectedObject) {
            this.className = detectedObject.getClassName();
            this.probability = detectedObject.getProbability();
            this.rect = ArrayUtils.toObject(detectedObject.getBoundingBox().getBounds().getCoordinates());
        }
    }
}
