package com.hmdp;

import com.hmdp.entity.Shop;
import com.hmdp.service.IShopService;
import com.hmdp.utils.RedisIdWorker;
import lombok.extern.slf4j.Slf4j;
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

    /**
     * 导入店铺数据到 geo
     */
    @Test
    void importData(){
        //查询数据
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

                locations.add(new RedisGeoCommands.GeoLocation<>(shop.getId().toString(),
                        new Point(shop.getX(), shop.getY())));
            }
            stringRedisTemplate.opsForGeo().add(SHOP_GEO_KEY + typeId,locations);
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
