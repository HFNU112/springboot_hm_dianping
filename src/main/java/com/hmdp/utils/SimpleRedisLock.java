package com.hmdp.utils;

import cn.hutool.core.lang.UUID;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * @Author Husp
 * @Date 2023/11/12 21:20
 */
public class SimpleRedisLock implements ILock {

    private static final String LOCK_PREFIX_KEY = "lock:prefix:";

    private static final String ID_PREFIX_KEY = UUID.randomUUID().toString(true) + ":";

    private static final DefaultRedisScript<Long> DEL_LOCK_SCRIPT_KEY;

    private String name;

    private StringRedisTemplate stringRedisTemplate;

    public SimpleRedisLock(String name, StringRedisTemplate stringRedisTemplate) {
        this.name = name;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    static {
        DEL_LOCK_SCRIPT_KEY = new DefaultRedisScript<Long>();
        DEL_LOCK_SCRIPT_KEY.setLocation(new ClassPathResource("delLock.lua"));
        DEL_LOCK_SCRIPT_KEY.setResultType(Long.class);
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
        //读取delLock.lua文件
        stringRedisTemplate.execute(DEL_LOCK_SCRIPT_KEY,
                Collections.singletonList(LOCK_PREFIX_KEY + name),
                ID_PREFIX_KEY + Thread.currentThread().getId());
    }

    /*@Override
    public void delLock() {
        //获取线程标识
        String threadId = ID_PREFIX_KEY + Thread.currentThread().getId();
        //获取redis中锁标识
        String id = stringRedisTemplate.opsForValue().get(LOCK_PREFIX_KEY + name);
        //判断线程标识
        if (threadId.equals(id)) {
            stringRedisTemplate.delete(LOCK_PREFIX_KEY + name);
        }
    }*/
}
