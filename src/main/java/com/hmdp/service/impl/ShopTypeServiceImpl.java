package com.hmdp.service.impl;

import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.CACHE_SHOP_TYPE_KEY;
import static com.hmdp.utils.RedisConstants.CACHE_SHOP_TYPE_TTL;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 查询店铺类型
     */
    @Override
    public Result queryTypeList() {
        UserDTO user = UserHolder.getUser();
        if (user == null){
            return null;
        }
        Long userId = user.getId();
        //创建List集合存储店铺类型
        List<ShopType> shopTypes = new ArrayList<>();
        //1.从redis中查询店铺类型
        List<String> shopTypeJsons = stringRedisTemplate.opsForList().range(CACHE_SHOP_TYPE_KEY + userId, 0, -1);
        //2.判断是否命中
        if (shopTypeJsons.size() != 0){
            //命中，遍历集合返回数据
            for (String shopTypeJson : shopTypeJsons) {
                ShopType shopType = JSONUtil.toBean(shopTypeJson, ShopType.class);
                shopTypes.add(shopType);
            }
            return Result.ok(shopTypes);
        }
        //3.未命中，查询数据库 select * from tb_shop_type order by sort desc
        List<ShopType> shopTypeList = query().orderByAsc("sort").list();
        //4.判断数据库是否存在店铺类型
        if (shopTypeList.size() != 0){
            //存在，遍历List集合写入redis
            for (ShopType shopType : shopTypeList) {
                stringRedisTemplate.opsForList().rightPushAll(CACHE_SHOP_TYPE_KEY + UserHolder.getUser().getId(),JSONUtil.toJsonStr(shopType));
                stringRedisTemplate.expire(CACHE_SHOP_TYPE_KEY, CACHE_SHOP_TYPE_TTL, TimeUnit.MINUTES);
            }
        }
        //返回数据
        return Result.ok(shopTypeList);
    }
}
