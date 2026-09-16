package com.ty.constant;

/**
 * Lucene 字段名常量
 *
 * @Author Tommy
 * @Date 2026/9/16
 */
public interface LuceneFields {

    /** 向量字段 */
    String EMBEDDING = "embedding";

    /** MD5 字段（用于去重和精确查询） */
    String MD5 = "md5";

    /** 图片名称字段（仅用于展示） */
    String NAME = "name";
}
