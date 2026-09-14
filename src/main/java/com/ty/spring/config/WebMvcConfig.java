package com.ty.spring.config;

import com.ty.spring.config.properties.TyProperties;
import com.ty.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

/**
 * Spring MVC配置类 等同于过去的XML配置方式
 *
 * @Author Tommy
 * @Date 2026/9/14
 */
@Configuration
@Slf4j
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private TyProperties tyProperties;

    /**
     * 配置视图控制器，用于实现简单的页面跳转
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        for (Map.Entry<String, String> entry : tyProperties.getViewMapping().entrySet()) {
            registry.addViewController(entry.getKey()).setViewName(entry.getValue());
        }
        log.info("配置的视图映射数量：" + tyProperties.getViewMapping().size() + " 条.");
    }

    /**
     * 添加各种时间类型Converter
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        // Date反序列化
        registry.addConverter(new Converter<String, Date>() {
            @Override
            public Date convert(String source) {
                return DateUtils.toDate(source);
            }
        });

        // LocalDate反序列化
        registry.addConverter(new Converter<String, LocalDate>() {
            @Override
            public LocalDate convert(String source) {
                source = org.apache.commons.lang3.StringUtils.replace(source, "/", "-");
                return LocalDate.parse(source, DateTimeFormatter.ofPattern(DateUtils.DEFAULT_DATE_FORMAT));
            }
        });

        // LocalTime反序列化
        registry.addConverter(new Converter<String, LocalTime>() {
            @Override
            public LocalTime convert(String source) {
                return LocalTime.parse(source, DateTimeFormatter.ofPattern(DateUtils.DEFAULT_TIME_FORMAT));
            }
        });

        // LocalDateTime反序列化
        registry.addConverter(new Converter<String, LocalDateTime>() {
            @Override
            public LocalDateTime convert(String source) {
                source = org.apache.commons.lang3.StringUtils.replace(source, "/", "-");
                return LocalDateTime.parse(source, DateTimeFormatter.ofPattern(DateUtils.DEFAULT_DATE_TIME_FORMAT));
            }
        });
    }
}
