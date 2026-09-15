package com.ty.spring.config;

import com.ty.spring.config.properties.AInferThreadPoolProperties;
import com.ty.spring.config.properties.TyProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 项目配置类
 *
 * @Author Tommy
 * @Date 2026/9/14
 */
@Configuration
@EnableConfigurationProperties({ TyProperties.class, AInferThreadPoolProperties.class })
@Slf4j
public class TyConfig {

    /**
     * AI推理任务线程池配置
     */
    @Bean
    public ThreadPoolTaskExecutor aiInferExecutor(AInferThreadPoolProperties poolProperties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(poolProperties.getCorePoolSize());         // 核心线程数
        executor.setMaxPoolSize(poolProperties.getMaxPoolSize());           // 最大线程数
        executor.setQueueCapacity(poolProperties.getQueueCapacity());       // 队列容量
        executor.setKeepAliveSeconds(poolProperties.getKeepAliveSeconds()); // 线程存活时间
        executor.setThreadNamePrefix(poolProperties.getThreadNamePrefix()); // 线程名前缀
        // 拒绝策略：由调用线程（Web线程）直接执行，实现天然背压
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 应用关闭时等待所有任务完成，优雅停机
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);

        log.info("AI推理任务线程池配置完毕: {}", poolProperties);
        return executor;
    }
}
