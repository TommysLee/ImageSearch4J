package com.ty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Spring Boot启动类
 *
 * @Author Tommy
 * @Date 2026/9/12
 */
@SpringBootApplication // 默认是扫描当前包以及子包的所有类
@EnableAsync // 启动异步调用
public class BootApplication {

    public static void main(String[] args) {
        SpringApplication.run(BootApplication.class, args);
    }
}
