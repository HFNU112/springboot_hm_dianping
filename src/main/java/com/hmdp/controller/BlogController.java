package com.hmdp.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Blog;
import com.hmdp.entity.User;
import com.hmdp.service.IBlogService;
import com.hmdp.service.IUserService;
import com.hmdp.utils.SystemConstants;
import com.hmdp.utils.UserHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Api(tags = "探店笔记数据接口")
@RestController
@RequestMapping("/blog")
public class BlogController {

    @Resource
    private IBlogService blogService;

    /**
     * 发布笔记
     * @param blog
     * @return
     */
    @ApiOperation(value = "发布笔记")
    @PostMapping
    public Result saveBlog(@RequestBody @ApiParam(value = "探店笔记请求对象") Blog blog) {
        return blogService.saveBlog(blog);
    }

    /**
     * 修改笔记点赞状态
     * @param id
     * @return
     */
    @ApiOperation(value = "修改笔记点赞状态")
    @PutMapping("/like/{id}")
    public Result likeBlog(@PathVariable("id") @ApiParam(value = "笔记id") Long id) {
        return blogService.updateLikeBlog(id);
    }

    /**
     * 查看我的笔记
     * @param current
     * @return
     */
    @ApiOperation(value = "查看我的笔记")
    @GetMapping("/of/me")
    public Result queryMyBlog(@RequestParam(value = "current", defaultValue = "1") @ApiParam(value = "当前页", example = "1") Integer current) {
        // 获取登录用户
        UserDTO user = UserHolder.getUser();
        // 根据用户查询
        Page<Blog> page = blogService.query()
                .eq("user_id", user.getId()).page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        return Result.ok(records);
    }

    /**
     * 查询热门笔记列表
     * TODO 补充需求分页查询笔记加redis 缓存
     * @param current
     * @return
     */
    @ApiOperation(value = "查询热门笔记列表")
    @GetMapping("/hot")
    public Result queryHotBlog(@RequestParam(value = "current", defaultValue = "1") @ApiParam(value = "当前页", example = "1") Integer current) {
        return blogService.queryHotBlog(current);
    }

    /**
     * 查询发布的笔记
     * TODO 补充需求笔记缓存加到redis
     * @param id
     * @return
     */
    @ApiOperation(value = "查询发布的笔记")
    @GetMapping("{id}")
    public Result queryBlogById(@PathVariable("id") @ApiParam(value = "笔记id") Long id){
        return blogService.queryBlogById(id);
    }

    /**
     * 查看笔记点赞列表
     * @param id
     * @return
     */
    @ApiOperation(value = "查看笔记点赞列表")
    @GetMapping("/likes/{id}")
    public Result queryBlogLikes(@PathVariable("id") @ApiParam(value = "笔记id") Long id){
        return blogService.queryBlogLikes(id);
    }

    /**
     * 查看发布人发布笔记
     */
    @ApiOperation(value = "查看发布人发布笔记")
    @GetMapping("/of/user")
    public Result queryBlogByUserId(@RequestParam("id") @ApiParam(value = "发笔记人的id") Long userId,
                                    @RequestParam(value = "current", defaultValue = "1") @ApiParam(value = "当前页") Integer current){
        return blogService.queryBlogByUserId(userId, current);
    }

    /**
     * 查询关注用户的笔记
     */
    @ApiOperation(value = "查询关注用户的笔记")
    @GetMapping("/of/follow")
    public Result queryBlogOfFollow(@RequestParam("lastId") @ApiParam(value = "上一次查询的最小时间戳") Long max,
                                    @RequestParam(value = "offset", defaultValue = "0") @ApiParam(value = "偏移量") Integer offset){
        return blogService.queryBlogOfFollow(max, offset);
    }

}
