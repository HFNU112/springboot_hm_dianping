package com.hmdp;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

/**
 * @Author Husp
 * @Date 2023/12/4 23:01
 */
@Slf4j
@SpringBootTest
public class JedisTest {

    private Jedis jedis;

    @BeforeEach
    void setUp(){
        // 建立连接
        jedis = new Jedis("192.168.241.128", 6379);
        //密码
        jedis.auth("123456");
        //选择库
        jedis.select(1);
    }

    @DisplayName("MSET 批量处理")
    @Test
    void testMset(){
        String[] arr = new String[2000];
        int j;
        long begin = System.currentTimeMillis();
        for (int i = 1; i <= 100000; i++) {
            j = (i % 1000) << 1;
            arr[j] = "test:key_:" + i;
            arr[j + 1] = "value_" + i;
            if (j == 0) {
                jedis.mset(arr);
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("time:" + (end - begin));//198
    }

    @DisplayName("pipeline批处理")
    @Test
    void testPipeline(){
        Pipeline pipeline = jedis.pipelined();
        long begin = System.currentTimeMillis();
        for (int i = 1; i <= 100000; i++) {
            pipeline.set("test:key_:" + i, "value_" + i);
            if (i % 1000 == 0) {
                pipeline.sync();
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("time:" + (end - begin)); //305
    }

    @AfterEach
    void tearDown(){
        if (jedis != null) {
            jedis.close();
        }
    }
}
