package com.hmdp.controller;


import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.service.IShopTypeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Api(tags = "店铺类型接口")
@RestController
@RequestMapping("/shop-type")
public class ShopTypeController {
    @Resource
    private IShopTypeService typeService;

    /**
     * 查询店铺类型
     */
    @ApiOperation(value = "查询店铺类型")
    @GetMapping("list")
    public Result queryTypeList() {
        return typeService.queryTypeList();
    }

    /**
     * 添加店铺新品种类型
     */
    @ApiOperation(value = "添加店铺新品种类型")
    @PostMapping
    public Result saveShopType(@RequestBody ShopType shopType){
        return typeService.saveShopType(shopType);
    }
}
