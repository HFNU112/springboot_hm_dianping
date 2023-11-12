package com.hmdp.utils;

import cn.hutool.core.lang.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * @Author Husp
 * @Date 2023/11/12 21:20
 */
public class SimpleRedisLock implements ILock {

    private static final String LOCK_PREFIX_KEY = "lock:prefix:";

    private static final String ID_PREFIX_KEY = UUID.randomUUID().toString(true) + ":";

    private String name;

    private StringRedisTemplate stringRedisTemplate;

    public SimpleRedisLock(String name, StringRedisTemplate stringRedisTemplate) {
        this.name = name;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean tryLock(Long timeoutSec) {
        String threadId = ID_PREFIX_KEY + Thread.currentThread().getId();
        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(LOCK_PREFIX_KEY + name, threadId + " ", timeoutSec, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }

    @Override
    public void delLock() {
        //获取线程标识
        String threadId = ID_PREFIX_KEY + Thread.currentThread().getId();
        //获取redis中锁标识
        String id = stringRedisTemplate.opsForValue().get(LOCK_PREFIX_KEY + name);
        //判断线程标识
        if (threadId.equals(id)) {
            stringRedisTemplate.delete(LOCK_PREFIX_KEY + name);
        }
    }
}
