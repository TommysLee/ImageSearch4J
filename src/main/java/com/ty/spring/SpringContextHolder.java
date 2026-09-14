package com.ty.spring;

import com.google.common.collect.Maps;
import com.ty.utils.WebUtil;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.LocaleResolver;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Spring容器工具类，方便在非spring管理环境中获取bean
 *
 * 此种实现方式更为通用化，兼顾Spring普通项目与Spring Boot项目
 *
 * @Author Tommy
 * @Date 2022/10/17
 */
@Component
public class SpringContextHolder {

    private static ApplicationContext applicationContext;
    private static DefaultListableBeanFactory defaultListableBeanFactory;
    private static LocaleResolver localeResolver;
    private static ResourceBundleMessageSource messageSource;
    private static Method getResourceBundleMethod;

    public void setApplicationContext(ApplicationContext applicationContext) {
        SpringContextHolder.applicationContext = applicationContext;
        SpringContextHolder.defaultListableBeanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        SpringContextHolder.localeResolver = applicationContext.getBean(LocaleResolver.class);
        SpringContextHolder.messageSource = applicationContext.getBean(ResourceBundleMessageSource.class);
        getResourceBundleMethod = ReflectionUtils.findMethod(messageSource.getClass(), "getResourceBundle", String.class, Locale.class);
        ReflectionUtils.makeAccessible(getResourceBundleMethod);
    }

    /**
     * 获取Spring上下文
     *
     * @return ApplicationContext
     */
    public static ApplicationContext getApplicationContext() {
        checkApplicationContext();
        return applicationContext;
    }

    /**
     * 获取Spring管理的Bean对象
     *
     * @param name
     * @return <T> T
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
        checkApplicationContext();
        return (T) applicationContext.getBean(name);
    }

    /**
     * 获取Spring管理的Bean对象
     *
     * @param clazz
     * @return <T> T
     */
    public static <T> T getBean(Class<T> clazz) {
        checkApplicationContext();
        return applicationContext.getBean(clazz);
    }

    /**
     * 将Bean注册到Spring上下文
     *
     * @param beanName
     * @param bean
     */
    public static <T> void setBean(String beanName, T bean) {
        checkBeanFactory();
        defaultListableBeanFactory.registerSingleton(beanName, bean);
    }

    /**
     * 从Spring上下文移除Bean
     *
     * @param beanName
     */
    public static void removeBean(String beanName) {
        checkBeanFactory();
        defaultListableBeanFactory.removeBeanDefinition(beanName);
    }

    /**
     * 发布Spring Event
     *
     * @param event
     */
    public static void publish(ApplicationEvent event) {
        checkApplicationContext();
        applicationContext.publishEvent(event);
    }

    /**
     * 从Http Request对象中获取Locale
     *
     * @return Locale
     */
    public static Locale getLocale() {
        return localeResolver.resolveLocale(WebUtil.getHttpRequest());
    }

    /**
     * 获取Message信息
     *
     * @param code
     * @return String
     */
    public static String getMessage(String code) {
        return applicationContext.getMessage(code, null, getLocale());
    }

    /**
     * 获取资源包的所有内容
     *
     * @param baseName
     * @return Map<String, String>
     */
    public static Map<String, String> getResourceBundle(String baseName) {
        return getResourceBundle(baseName, getLocale());
    }

    /**
     * 获取资源包的所有内容
     *
     * @param baseName
     * @param locale
     * @return Map<String, String>
     */
    public static Map<String, String> getResourceBundle(String baseName, Locale locale) {
        Map<String, String> bundleMap = Maps.newHashMap();
        ResourceBundle bundle = (ResourceBundle) ReflectionUtils.invokeMethod(getResourceBundleMethod, messageSource, baseName, locale);
        if (null != bundle) {
            bundle.keySet().stream().forEach(k -> bundleMap.put(k, bundle.getString(k)));
        }
        return bundleMap;
    }

    /**
     * 清除Spring上下文
     */
    public static void cleanApplicationContext() {
        applicationContext = null;
    }

    /**
     * 检查ApplicationContext
     */
    private static void checkApplicationContext() {
        if (applicationContext == null) {
            throw new IllegalStateException("applicaitonContext未注入,请在applicationContext.xml中定义SpringContextHolder");
        }
    }

    /**
     * 检查DefaultListableBeanFactory
     */
    private static void checkBeanFactory() {
        if (null == defaultListableBeanFactory) {
            throw new IllegalStateException("DefaultListableBeanFactory未注入,请在applicationContext.xml中定义SpringContextHolder");
        }
    }
}
