package com.ty.model;

import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects.DetectedObject;
import lombok.Data;

import java.util.List;

/**
 * 以图搜图结果
 *
 * <p>封装以图搜图请求的完整信息，包含：
 * <ul>
 *   <li>主体检测的候选集（供前端展示"识别到了什么"）</li>
 *   <li>最终选定的目标框（供前端标注）</li>
 *   <li>向量匹配结果（核心输出）</li>
 * </ul>
 *
 * <p>设计原则：即便向量匹配失败（{@code match == null}），
 * 前端依然可以通过 {@code candidates} 与 {@code bbox} 向用户展示检测结果，
 * 让用户知道"检测到了，只是向量库中没有匹配项"。
 */
@Data
public class SearchResult {

    /** 主体检测的候选集 **/
    private List<DetectedObject> candidates;

    /** 最终选定的目标框 **/
    private BoundingBox bbox;

    /** 向量匹配结果 **/
    private VectorDocument match;
}
