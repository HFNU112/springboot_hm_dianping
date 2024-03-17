package com.hmdp;

import com.hmdp.entity.Shop;
import com.hmdp.service.IShopService;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.RedisIdWorker;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.hmdp.utils.RedisConstants.SHOP_GEO_KEY;

@Slf4j
@SpringBootTest
class HmDianPingApplicationTests {

    @Resource
    private IShopService shopService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private RedissonClient redissonClient2;

    @Resource
    private RedissonClient redissonClient3;

    private RLock lock;

    @BeforeEach
    void setUp(){
        RLock lock1 = redissonClient.getLock("lock");
        RLock lock2 = redissonClient.getLock("lock");
        RLock lock3 = redissonClient.getLock("lock");
        //创建联锁
        lock = redissonClient.getMultiLock(lock1, lock2, lock3);
    }

    /**
     * 导入店铺数据到 geo
     */
    @Test
    @DisplayName("导入店铺数据到操作成功")
    void importData(){
        //查询所有店铺数据
        List<Shop> shopList = shopService.list();

        // 按typeId分组
        Map<Long, List<Shop>> shopMap = shopList.stream().collect(Collectors.groupingBy(Shop::getTypeId));

        // 依次存入到redis
        for (Map.Entry<Long, List<Shop>> shopEntry : shopMap.entrySet()) {
            //获取类型id
            Long typeId = shopEntry.getKey();
            List<Shop> value = shopEntry.getValue();
            List<RedisGeoCommands.GeoLocation<String>> locations = new ArrayList<>(value.size());
            //获取店铺集合
            for (Shop shop : value) {
                // 每一条附近店铺信息写入redis
//                stringRedisTemplate.opsForGeo().add(SHOP_GEO_KEY + typeId, new Point(shop.getX(), shop.getY()), shop.getId().toString());
                locations.add(new RedisGeoCommands.GeoLocation<>(shop.getId().toString(), new Point(shop.getX(), shop.getY())));
            }
            stringRedisTemplate.opsForGeo().add(RedisConstants.SHOP_GEO_KEY + typeId,locations);
            stringRedisTemplate.expire(RedisConstants.SHOP_GEO_KEY + typeId, RedisConstants.SHOP_GEO_TTL, TimeUnit.DAYS);
        }
    }

    @Test
    void testRedisson() throws InterruptedException {
        //获取可重入锁
        RLock rLock = redissonClient.getLock("RLock");
        //尝试获取锁
        boolean isLock = rLock.tryLock(1, 10, TimeUnit.SECONDS);
        if (isLock){
            try {
                log.info("执行业务代码");
            } finally {
                rLock.unlock();
            }
        }

    }

}
