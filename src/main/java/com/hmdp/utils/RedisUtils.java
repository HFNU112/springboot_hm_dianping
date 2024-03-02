package com.hmdp.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * redis命令行操作工具类
 *
 * @Author Husp
 * @Date 2024/1/14 17:42
 */
@Slf4j
public class RedisUtils {

    @Resource
    public StringRedisTemplate stringRedisTemplate;

    private static StringRedisTemplate redisTemplate;

    public RedisUtils(){
        redisTemplate = this.stringRedisTemplate;
    }

    /**
     * 指定缓存失效时间 EXPIRE key seconds
     *
     * @param key
     * @param time
     * @return
     */
    public static boolean expire(String key, long time) {
        // 判断缓存key是否存在
        if (!isExist(key)) {
            return false;
        }
        // 设置key的缓存过期时间
        return Boolean.TRUE.equals(redisTemplate.expire(key, time, TimeUnit.SECONDS));
    }


    /**
     * 根据key获取过期时间
     *
     * @param key
     * @return 时间(秒) 返回-1代表为永久有效
     */
    public static Long getExpire(String key) {
        // 判断缓存key是否存在
        return (!isExist(key)) ? null : redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 判断key是否存在
     *
     * @param key 缓存key
     * @return true 缓存存在 false 缓存不存在
     */
    public static boolean isExist(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("key不存在");
            return false;
        }
    }

    // ============================String=============================

    /**
     * 设置key以保存字符串值
     *
     * @param key
     */
    public static void set(String key) {

    }

    /**
     * 获取key的值
     *
     * @param key
     * @return
     */
    public static String get(String key) {
        return null;
    }

    // ============================Sorted Set=============================

    /**
     * 指定分数的所有指定成员添加到存储在key处的Sorted Set集
     *
     * @param key
     * @param values
     * @return
     */
    public static boolean zadd(String key, Object values) {
        return true;
    }

    /**
     * 按索引(排名)、按分数或按字典顺序查询Sorted Set
     * @param key
     * @param start
     * @param end
     * @return
     */
    public static Set<Object> zrange(String key, long start, long end){
        return null;
    }


}
