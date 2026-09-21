package com.ty.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 向量Document
 *
 * <p>本类是 {@link com.ty.service.VectorService} 的返回对象，封装了 Lucene {@code Document} 的存储字段与相似度分数。
 *
 * @Author Tommy
 * @Date 2026/9/17
 */
@Data
@AllArgsConstructor
public class VectorDocument implements Serializable {

    @Serial
    private static final long serialVersionUID = 666877171855781278L;

    /** 图片名称 **/
    private String name;

    /** 图片路径 **/
    private String path;

    /** 图片MD5 **/
    private String md5;

    /** 相似度得分 **/
    private float score;

    /** 与该文档对应的查询特征向量 **/
    @JsonIgnore
    private float[] queryVector;
}
