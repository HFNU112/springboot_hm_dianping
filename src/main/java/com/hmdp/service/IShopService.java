package com.hmdp.service;

import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IShopService extends IService<Shop> {

    /**
     * 根据id查询商铺信息
     */
    Result queryShopById(Long id);

    /**
     * 更新商铺信息
     */
    Result updateShop(Shop shop);

    /**
     * 根据商铺类型分页查询商铺信息
     */
    Result queryShopByType(Integer typeId, Integer current);

}
