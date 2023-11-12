package com.hmdp.utils;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * @Author Husp
 * @Date 2023/11/12 21:20
 */
public class SimpleRedisLock implements ILock{

    private static final String LOCK_PREFIX_KEY = "lock:prefix:";

    private String name;

    private StringRedisTemplate stringRedisTemplate;

    public SimpleRedisLock(String name, StringRedisTemplate stringRedisTemplate) {
        this.name = name;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean tryLock(Long timeoutSec) {
        long threadId = Thread.currentThread().getId();
        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(LOCK_PREFIX_KEY + name, threadId + " ", timeoutSec, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }

    @Override
    public void delLock() {
        stringRedisTemplate.delete(LOCK_PREFIX_KEY + name);
    }
}
