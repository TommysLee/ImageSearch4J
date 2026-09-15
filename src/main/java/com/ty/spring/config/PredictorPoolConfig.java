package com.ty.spring.config;

import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.ZooModel;
import com.ty.spring.config.properties.PredictorPoolProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Predictor对象池配置类
 *
 * @Author Tommy
 * @Date 2026/9/15
 */
@Configuration
@EnableConfigurationProperties(PredictorPoolProperties.class)
@Slf4j
public class PredictorPoolConfig {

    /**
     * Predictor主体检测对象池配置
     */
    @Bean
    public GenericObjectPool<Predictor<Image, DetectedObjects>> mainbodyPredictorPool(ZooModel<Image, DetectedObjects> mainbodyModel, PredictorPoolProperties poolProperties) {
        GenericObjectPool<Predictor<Image, DetectedObjects>> pool = buildPool(mainbodyModel, poolProperties);
        log.info("Predictor 主体检测对象池配置完毕: {}", poolProperties);
        return pool;
    }

    /**
     * Predictor特征向量提取对象池配置
     */
    @Bean
    public GenericObjectPool<Predictor<Image, float[]>> generalPPLCNetPredictorPool(ZooModel<Image, float[]> generalPPLCNetModel, PredictorPoolProperties poolProperties) {
        GenericObjectPool<Predictor<Image, float[]>> pool = buildPool(generalPPLCNetModel, poolProperties);
        log.info("Predictor 特征向量提取对象池配置完毕: {}", poolProperties);
        return pool;
    }

    /**
     * 构建对象池
     */
    private <I, O> GenericObjectPool<Predictor<I, O>> buildPool(ZooModel<I, O> model, PredictorPoolProperties poolProperties) {
        // 自定义池化工厂，负责创建和销毁 Predictor 实例
        BasePooledObjectFactory<Predictor<I, O>> factory = new BasePooledObjectFactory<>() {
            @Override
            public Predictor<I, O> create() throws Exception {
                return model.newPredictor();
            }

            @Override
            public PooledObject<Predictor<I, O>> wrap(Predictor<I, O> predictor) {
                return new DefaultPooledObject<>(predictor);
            }

            @Override
            public void destroyObject(PooledObject<Predictor<I, O>> p) throws Exception {
                // 销毁时关闭 Predictor，释放 Native 资源
                try {
                    p.getObject().close();
                } catch (Exception e) {
                    log.warn("Failed to close Predictor", e);
                }
            }
        };

        // 池化配置
        GenericObjectPoolConfig<Predictor<I, O>> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(poolProperties.getMaxTotal());   // 最大实例数
        config.setMaxIdle(poolProperties.getMaxIdle());     // 最大空闲实例数
        config.setMinIdle(poolProperties.getMinIdle());     // 最小空闲实例数
        config.setBlockWhenExhausted(poolProperties.isBlockWhenExhausted());    // 池耗尽时是否阻塞等待
        config.setMaxWait(Duration.ofSeconds(poolProperties.getMaxWait()));     // 阻塞等待的最大等待时长
        config.setTestOnBorrow(poolProperties.isTestOnBorrow()); // 关闭借出的有效性检查
        config.setTestOnReturn(poolProperties.isTestOnReturn()); // 关闭归还时的有效性检查
        config.setTimeBetweenEvictionRuns(Duration.ofSeconds(poolProperties.getTimeBetweenEvictionRuns())); // 后台扫描空闲实例的轮询时间
        config.setMinEvictableIdleDuration(Duration.ofSeconds(poolProperties.getMinEvictableIdleTime()));   // 空闲实例超过此时间自动回收
        config.setJmxEnabled(false);   // 关闭 JMX，避免多池默认名冲突

        // 池化对象
        return new GenericObjectPool<>(factory, config);
    }
}
