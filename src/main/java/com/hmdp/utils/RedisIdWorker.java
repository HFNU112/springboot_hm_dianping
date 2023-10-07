package com.hmdp.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * 全局唯一id生成器
 * @Author Husp
 * @Date 2023/10/7
 */
@Slf4j
@Component
public class RedisIdWorker {

    /**
     * 开始时间戳  2022-01-01 00:00:00 31bit  1bit符号位
     */
    private static final long BEGIN_TIMESTAMP = 1672531200L;

    /**
     * 序列号位数 32bit
     */
    private static final long SEQ_BITS = 32L;

    private StringRedisTemplate stringRedisTemplate;

    public RedisIdWorker(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 生成订单下一位ID
     * @param keyPrefix 每一个订单业务的key前缀
     * @return 订单下一位ID
     */
    public long nextId (String keyPrefix){
        //1. 当前时间戳
        LocalDateTime currentTime = LocalDateTime.now();
        long currentSecond = currentTime.toEpochSecond(ZoneOffset.UTC);
        long currentTimeStamp = currentSecond - BEGIN_TIMESTAMP;

        //2. 序列号
        //获取当前时间 20231007
        String date = currentTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        //序列号自增
        Long seqNumber = stringRedisTemplate.opsForValue().increment("INCR:" + keyPrefix + ":" +date);

        //3. 返回拼接后的时间戳和序列号  位运算拼接
        return currentTimeStamp << SEQ_BITS | seqNumber;
    }

}
