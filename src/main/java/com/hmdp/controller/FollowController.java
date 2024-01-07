package com.hmdp.controller;


import com.hmdp.dto.Result;
import com.hmdp.service.IFollowService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Api(tags = "好友关注接口")
@RestController
@RequestMapping("/follow")
public class FollowController {

    @Resource
    private IFollowService followService;

    /**
     * 关注发笔记人
     */
    @ApiOperation(value = "关注发笔记人")
    @PutMapping("/{id}/{isFollow}")
    public Result follow(@PathVariable("id") @ApiParam(value = "关注发笔记的人id") Long followUserId,
                         @PathVariable("isFollow") @ApiParam(value = "是否被关注") Boolean isFollow){
        return followService.follow(followUserId, isFollow);
    }

    /**
     * 取消关注发笔记人
     */
    @ApiOperation(value = "取消关注发笔记人")
    @GetMapping("/or/not/{id}")
    public Result isFollow(@PathVariable("id") @ApiParam(value = "关注发笔记的人id") Long followUserId){
        return followService.isFollow(followUserId);
    }

    /**
     * 查看共同关注人信息
     */
    @ApiOperation(value = "查看共同关注人信息")
    @GetMapping("/common/{id}")
    public Result followCommon(@PathVariable("id") @ApiParam(value = "关注发笔记的人id") Long followUserId){
        return followService.followCommon(followUserId);
    }
}
