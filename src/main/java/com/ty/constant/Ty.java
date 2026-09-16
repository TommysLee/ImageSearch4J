package com.ty.constant;

/**
 * 项目常量
 *
 * @Author Tommy
 * @Date 2022/1/26
 */
public interface Ty {

    /** 默认字符集 **/
    String DEFAULT_CHARSET = "utf-8";

    /** 处理器数量 **/
    int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

    /** 用户根目录 **/
    String USER_HOME = System.getProperty("user.home");

    /** 数据总记录数Key **/
    String TOTAL = "total";

    /** 数据结果集Key **/
    String DATA = "data";

    /** 总页数Key **/
    String PAGES = "pages";

    /** 默认页码 **/
    String DEFAULT_PAGE = "1";

    /** 默认每页显示条数 **/
    String DEFAULT_PAGESIZE = "20";

    /** 正则转义符 **/
    String ESCAPE = "\\";

    /** 竖线分隔符 **/
    String DELIMITER_VLINE = ESCAPE + "|";

    /** 逗号 **/
    String COMMA = ",";

    /** 分号 **/
    String SEMICOLON = ";";

    /** 点号 **/
    String POINT = ".";

    /** 横线 **/
    String HLINE = "-";

    /** 美元符号 **/
    String DOLLAR = "$";

    /** 斜杠 **/
    String SLASH = "/";

    /** 星号 **/
    String ASTERISK = "*";

    /** 默认缓存区大小 **/
    int DEFAULT_BUFFER_SIZE = 4096;
}
