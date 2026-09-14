package com.ty.spring.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static com.ty.constant.Ty.AVAILABLE_PROCESSORS;

/**
 * AI推理任务的线程池属性配置类
 *
 * @Author Tommy
 * @Date 2026/9/15
 */
@ConfigurationProperties(prefix = "ty.ai-infer")
@Data
public class AInferThreadPoolProperties {

    /** 推理任务：核心线程数：根据GPU/CPU算力设置，GPU推理建议设为4~8，CPU密集型设为核心数+1 **/
    private int corePoolSize = AVAILABLE_PROCESSORS;

    /** 推理任务：最大线程数：不超过核心数2倍，防止上下文切换过载 **/
    private int maxPoolSize = AVAILABLE_PROCESSORS * 2;

    /** 推理任务：队列容量：积压请求上限，超过直接触发拒绝策略 **/
    private int queueCapacity = 100;

    /** 推理任务：线程存活时间：非核心线程空闲时间到后回收 **/
    private int keepAliveSeconds = 60;

    /** 推理任务：线程名前缀：方便日志排查 **/
    private String threadNamePrefix = "ai-infer-";
}
