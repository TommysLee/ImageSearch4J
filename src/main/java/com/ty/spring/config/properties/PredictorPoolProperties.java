package com.ty.spring.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static com.ty.constant.Ty.AVAILABLE_PROCESSORS;

/**
 * Predictor 对象池属性配置类
 *
 * @Author Tommy
 * @Date 2026/9/15
 */
@ConfigurationProperties(prefix = "ty.predictor")
@Data
public class PredictorPoolProperties {

    /** 最大实例数：和 aiInferExecutor 最大线程数保持一致，防止 GPU 显存溢出 **/
    private int maxTotal = AVAILABLE_PROCESSORS * 2;

    /** 最大空闲实例数：避免频繁销毁重建 **/
    private int maxIdle = AVAILABLE_PROCESSORS * 2;

    /** 最小空闲实例数：常驻的预热实例数，消除冷启动延迟 **/
    private int minIdle = AVAILABLE_PROCESSORS;

    /** 池耗尽时是否阻塞等待 **/
    private boolean blockWhenExhausted = true;

    /** 阻塞等待的最大等待时长（单位：秒） **/
    private int maxWait = 1;

    /** 关闭借出的有效性检查（Predictor 通常不会损坏，节省开销） **/
    private boolean testOnBorrow = false;

    /** 关闭归还时的有效性检查 **/
    private boolean testOnReturn = false;

    /** 后台扫描空闲实例的轮询时间，销毁超时对象（单位：秒） **/
    private int timeBetweenEvictionRuns = 30;

    /** 空闲实例超过此时间自动回收（单位：秒） **/
    private int minEvictableIdleTime = 300;
}
