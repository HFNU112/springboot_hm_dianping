package com.hmdp;

import com.hmdp.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;

import static com.hmdp.utils.RedisConstants.CACHE_SHOP_TYPE_KEY;

/**
 * @Author Husp
 * @Date 2024/1/21 15:13
 */
@Slf4j
@SpringBootTest(classes = HmDianPingApplication.class)
public class TestRedisCommand {

    private RedisTemplate<String, Object> redisTemplate;

    @BeforeEach
    void setUp(){

    }

    @Test
    @DisplayName("判断key是否存在")
    public void testIsExist(){
        String key = CACHE_SHOP_TYPE_KEY + 1016;
        boolean success = RedisUtils.isExist(key);
        Assertions.assertTrue(success);
    }


    @AfterEach
    void tearDown() throws IOException {

    }
}
