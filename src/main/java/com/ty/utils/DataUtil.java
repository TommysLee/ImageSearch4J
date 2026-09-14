package com.ty.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.ty.spring.SpringContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 数据工具类
 *
 * @Author Tommy
 * @Date 2022/1/26
 */
@Slf4j
public class DataUtil {

    private static ObjectMapper mapper;
    private static JavaTimeModule javaTimeModule = new JavaTimeModule();

    static {
        try {
            mapper = SpringContextHolder.getBean(ObjectMapper.class);
            log.info("Jackson Init Based on SpringBoot");
        } catch (Exception e) {
            mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
            mapper.setTimeZone(TimeZone.getDefault());
            mapper.registerModule(getTimeModule());
            log.info("Jackson Init Based on Native");
        }
    }

    /**
     * 将JSON字符串转换为对象
     *
     * @param jsonText
     *                 ----> JSON字符串
     * @return 返回Map
     */
    @SuppressWarnings("unchecked")
    public static <T> Map<String, T> fromJSON(String jsonText) {

        return fromJSON(jsonText, Map.class);
    }

    /**
     * 将JSON字符串转换为对象
     *
     * @param jsonText
     *                 ----> JSON字符串
     * @param defaultVal
     *                 ----> 默认值
     * @return 返回Map
     */
    @SuppressWarnings("unchecked")
    public static <T> Map<String, T> fromJSON(String jsonText, Map<String, T> defaultVal) {

        if (StringUtils.isNotBlank(jsonText)) {
            return fromJSON(jsonText, Map.class);
        }
        return defaultVal;
    }

    /**
     * 将JSON字符串转换为对象
     *
     * @param jsonText
     *                 ----> JSON字符串
     * @param defaultVal
     *                 ----> 默认值
     * @return 返回Map
     */
    @SuppressWarnings("unchecked")
    public static <T> Map<Integer, T> fromOJSON(String jsonText, Map<Integer, T> defaultVal) {

        if (StringUtils.isNotBlank(jsonText)) {
            final Map<String, T> data = fromJSON(jsonText, LinkedHashMap.class);
            final Map<Integer, T> result = Maps.newLinkedHashMap();
            for (Map.Entry<String, T> entry : data.entrySet()) {
                result.put(Integer.parseInt(entry.getKey().trim()), entry.getValue());
            }
            return result;
        }
        return defaultVal;
    }

    /**
     * 将JSON字符串转换为对象
     *
     * @param jsonText
     *                 ----> JSON字符串
     * @param clazz
     *                 ----> Class<T>
     * @return <T> T
     */
    public static <T> T fromJSON(String jsonText, Class<T> clazz) {

        try {
            return mapper.readValue(jsonText, clazz);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将JSON数组转换为集合对象
     *
     * @param jsonArray JSON数组字符串
     * @param clazz
     * @param <T>
     * @return List<T>
     */
    public static <T> List<T> fromJSONArray(String jsonArray, Class<T> clazz) {
        try {
            if (StringUtils.isNotBlank(jsonArray)) {
                CollectionType javaType = mapper.getTypeFactory().constructCollectionType(List.class, clazz);
                return mapper.readValue(jsonArray, javaType);
            }
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
        }
        return Lists.newArrayList();
    }

    /**
     * 将对象转换为JSON字符串
     *
     * @param jsonObject
     *                   ----> 对象
     * @return String
     */
    public static String toJSON(Object jsonObject) {

        try {
            return mapper.writeValueAsString(jsonObject);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将Bean转换为Map
     *
     * @param bean
     * @return Map<String, Object>
     */
    public static Map<String, Object> toMap(Object bean) {

        Map<String, Object> beanMap = null;
        try {
            if (null != bean) {
                beanMap = mapper.convertValue(bean, HashMap.class);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return beanMap;
    }

    /**
     * 将Map转换为Bean
     *
     * @param beanMap
     * @param clazz
     * @return T
     */
    public static <T> T toBean(Map<String, Object> beanMap, Class<T> clazz) {
        T bean = null;
        try {
            if (null != beanMap && null != clazz) {
                bean = mapper.convertValue(beanMap, clazz);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return bean;
    }

    /**
     * 将Map转换为Bean
     *
     * @param beanMap
     * @param clazz
     * @param insideClazz
     * @return T
     */
    public static <T> T toBean(Map<String, Object> beanMap, Class<T> clazz, Class<?> insideClazz) {
        T bean = null;
        try {
            if (null != beanMap && null != clazz) {
                bean = mapper.convertValue(beanMap, mapper.getTypeFactory().constructParametricType(clazz, insideClazz));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return bean;
    }

    /**
     * char 转 byte[] 数组
     * @param c
     * @return
     */
    public static byte[] charToByte(char c) {
        byte[] b = new byte[2];
        b[0] = (byte) ((c & 0xFF00) >> 8);
        b[1] = (byte) (c & 0xFF);
        return b;
    }

    /**
     * 判断对象是否为Null
     *
     * @param obj
     * @return true / false
     */
    public static boolean isNull(Object obj) {
        return null == obj;
    }

    /**
     * 获取对象的所有Null属性
     *
     * @param source
     * @return String[]
     */
    public static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null || StringUtils.EMPTY.equals(srcValue))
                emptyNames.add(pd.getName());
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

    /**
     * 复制非空属性到目标对象
     *
     * @param src 源对象
     * @param target 目标对象
     */
    public static void copyPropertiesIgnoreNull(Object src, Object target) {
        BeanUtils.copyProperties(src, target, getNullPropertyNames(src));
    }

    /**
     * 返回入参的恰当的值
     *
     * @param val
     * @return Object
     */
    public static Object getFitValue(Object val) {
        if (null != val) {
            if (val instanceof Instant) {
                return val.toString();
            } else if (val instanceof Date) {
                Date dateVal = (Date) val;
                return dateVal.toInstant().toString();
            }
        }
        return val;
    }

    /**
     * 计算当前节点的所有后代节点的Level
     *
     * @param item  当前节点
     * @param itemLevel 当前节点的Level
     * @param childrenFunc  获取子节点的函数接口
     * @param setLevelConsumer  设置Level的函数接口
     * @return T 返回当前节点
     */
    public static <T> T calChildrenLevel(T item, int itemLevel, Function<T, List<T>> childrenFunc, BiConsumer<T, Integer> setLevelConsumer) {
        List<T> children = childrenFunc.apply(item);
        if (null != children && children.size() > 0) {
            children.stream().forEach(child -> {
                int level = itemLevel + 1;
                setLevelConsumer.accept(child, level);
                calChildrenLevel(child, level, childrenFunc, setLevelConsumer);
            });
        }
        return item;
    }

    /**
     * 通过数据列表构建任意树的数据
     *
     * @param dataList  列表数据
     * @param idFunc    获取PK的函数接口
     * @param parentIdFunc  获取父ID的函数接口
     * @param childrenFunc  获取子节点的函数接口
     * @param setLevelConsumer      设置Level的函数接口
     * @param setChildrenConsumer   设置子节点的函数接口
     * @param onlyRootNode  是否只返回根节点数据
     * @return List<T> 返回树结构数据集合
     */
    public static <T> List<T> wrapTreeData(List<T> dataList, Function<T, String> idFunc, Function<T, String> parentIdFunc, Function<T, List<T>> childrenFunc, BiConsumer<T, Integer> setLevelConsumer, BiConsumer<T, List<T>> setChildrenConsumer, boolean onlyRootNode) {

        Set<String> childIds = Sets.newHashSet();
        List<T> treeData = dataList.stream().map(currentItem -> { // 查找节点的所有子节点
            List<T> children = Lists.newArrayList();
            dataList.stream().forEach(item -> {
                if (idFunc.apply(currentItem).equals(parentIdFunc.apply(item))) {
                    children.add(item);
                    childIds.add(idFunc.apply(item));
                }
            });
            if (children.size() > 0) {
                setChildrenConsumer.accept(currentItem, children);
            }
            return currentItem;
        }).collect(Collectors.collectingAndThen(Collectors.toList(), resultList -> {
            return resultList.stream().filter(item -> { // 过滤所有子节点（因为子节点已被添加到 父节点的 children 属性）
                boolean flag = !childIds.contains(idFunc.apply(item));
                if (flag) {
                    setLevelConsumer.accept(item, 1); // 根节点 level=1
                    calChildrenLevel(item, 1, childrenFunc, setLevelConsumer); // 从每一个根节点开始，递归计算各子节点的层级
                }
                return flag;
            }).collect(Collectors.toList());
        }));

        if (onlyRootNode) { // 只返回根节点
            dataList.forEach(item -> setChildrenConsumer.accept(item, null));
        }
        return treeData;
    }

    /**
     * 获取Jackson JavaTimeModule
     *
     * @return JavaTimeModule
     */
    public static JavaTimeModule getTimeModule() {
        javaTimeModule.addSerializer(LocalDateTime.class,new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DateUtils.DEFAULT_DATE_TIME_FORMAT)));
        javaTimeModule.addSerializer(LocalDate.class,new LocalDateSerializer(DateTimeFormatter.ofPattern(DateUtils.DEFAULT_DATE_FORMAT)));
        javaTimeModule.addSerializer(LocalTime.class,new LocalTimeSerializer(DateTimeFormatter.ofPattern(DateUtils.DEFAULT_TIME_FORMAT)));
        javaTimeModule.addDeserializer(LocalDateTime.class,new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DateUtils.DEFAULT_DATE_TIME_FORMAT)));
        javaTimeModule.addDeserializer(LocalDate.class,new LocalDateDeserializer(DateTimeFormatter.ofPattern(DateUtils.DEFAULT_TIME_FORMAT)));
        return javaTimeModule;
    }
}
