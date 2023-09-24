package com.hmdp.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.entity.vo.RedisData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.hmdp.utils.RedisConstants.*;

/**
 * 重建缓存工具类
 *
 * @author: Husp
 * @date: 2023/9/24 21:20
 */
@Slf4j
@Component
public class CacheClient {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // 缓存重建线程池
    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    public CacheClient(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void set(String key, Object value, Long time, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, unit);
    }

    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit) {
        // 设置逻辑过期
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        // 写入Redis
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    public <R, ID> R queryWithLogicalExpire(String prefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit)
    {
        String key = prefix + id;
        //1.从redis中查询商铺
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        //2.判断缓存是否命中
        if (StrUtil.isBlank(shopJson)) {
            //缓存为空
            return null;
        }
        //命中，序列化shop对象
        RedisData redisData = JSONUtil.toBean(shopJson, RedisData.class);
        R r = JSONUtil.toBean((JSONObject) redisData.getData(), type);
        LocalDateTime expireTime = redisData.getExpireTime();
        if (LocalDateTime.now().isAfter(expireTime)) {
            // 没有过期，返回商铺
            return r;
        }
        // 过期，缓存重建
        String lockKey = LOCK_SHOP_KEY + id;
        // 缓存重构
        // 获取互斥锁
        Boolean isLock = tryLock(lockKey);
        //判断是否获取锁成功
        if (isLock) {
            // 失败，重新获取锁
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    // 查询数据库
                    R newr = dbFallback.apply(id);
                    //重建缓存
                    this.setWithLogicalExpire(key, newr, time, unit);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    delLock(lockKey);
                }
            });
        }
        //返回过期的商铺数据
        return r;
    }

    public <R, ID> R queryWithMutex(String prefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit)
    {
        String key = prefix + id;
        //1.从redis中查询商铺
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        //2.判断缓存是否命中
        if (StrUtil.isNotBlank(shopJson)) {
            //命中，直接返回数据
            return JSONUtil.toBean(shopJson, type);
        }
        if (shopJson == null) {
            return null;
        }
        String lockKey = LOCK_SHOP_KEY + id;
        R r = null;
        try {
            // 缓存重构
            // 获取互斥锁
            Boolean isLock = tryLock(lockKey);
            //判断是否获取锁成功
            if (!isLock) {
                // 失败，重新获取锁
                Thread.sleep(50);
                return queryWithMutex(prefix, id, type, dbFallback , time, unit);
            }
            //3.根据id查询数据库
            r = dbFallback.apply(id);
            //4.判断数据库数据是否存在
            if (r == null) {
                // 缓存null 解决缓存穿透
                stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
                //不存在，返回空
                return null;
            }
            //存在，写入redis
            this.set(key, r, time, unit);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            // 释放锁
            delLock(lockKey);
        }
        return r;
    }

    private Boolean tryLock(String lockKey) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "1", LOCK_SHOP_TTL, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    private void delLock(String lockKey) {
        stringRedisTemplate.delete(lockKey);
    }



}
