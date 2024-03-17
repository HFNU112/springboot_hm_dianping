package com.hmdp.utils;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 日期格式化工具类
 * @Author Husp
 * @Date 2024/1/14 16:47
 */
public class DateFormateUtils {

    /**
     * 时间格式(yyyy-MM-dd)前缀
     */
    public static final String DATE_PREFIX = "yyyy-MM-dd";

    /**
     * 时间格式(yyyy-MM-dd HH:mm:ss)前缀
     */
    public static final String DATE_TIME_PREFIX = "yyyy-MM-dd HH:mm:ss";

    /**
     * 日期格式化 日期格式为：yyyy-MM-dd
     * @param date
     * @return
     */
    public static String format(LocalDateTime date) {
        return format(date, DATE_PREFIX);
    }

    /**
     * 日期格式化 日期格式为：yyyy-MM-dd
     * @param date
     * @return
     */
    public static String format(LocalDateTime date, String pattern) {
        if(date != null){
            SimpleDateFormat df = new SimpleDateFormat(pattern);
            return df.format(date);
        }
        return null;
    }

}
