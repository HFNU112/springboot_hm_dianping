package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Blog;
import com.hmdp.mapper.BlogMapper;
import com.hmdp.service.IBlogService;
import com.hmdp.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result saveBlog(Blog blog) {
        // 获取登录用户
        UserDTO user = UserHolder.getUser();
        blog.setUserId(user.getId());
        // 保存探店博文
        save(blog);
        return Result.ok(blog.getId());
    }

    @Override
    public Result queryBlogById(Long id) {
        //1.从redis中查询Blog
        String blogJson = stringRedisTemplate.opsForValue().get(CACHE_BLOG_KEY + id);

        //2.判断缓存key是否命中
        if (StrUtil.isNotBlank(blogJson)){
            //命中，直接返回
            Blog blog = JSONUtil.toBean(blogJson, Blog.class);
            return Result.ok(blog);
        }
        //3.未命中，查询数据库Blog表
        Blog blog = getById(id);
        //4.判断数据库查询是否存在
        if (blog == null){
            //不存在直接返回404
            return Result.fail("当前尚未发布笔记");
        }
        //5.数据库存在并写入redis
        stringRedisTemplate.opsForValue().set(CACHE_BLOG_KEY + id, JSON.toJSONString(blog), CACHE_BLOG_TTL, TimeUnit.MINUTES);

        //8.返回Blog
        return Result.ok(blog);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result updateLikeBlog(Long id) {
        if (id == null){
            return Result.fail("笔记id不能为空！");
        }
        // 1.操作数据库 执行增、删、改 出现异常需要回滚事务update tb_blog set liked = liked + 1 where id = ?
        update().setSql("liked = liked + 1").eq("id", id).update();

        // 2.删除缓存
        stringRedisTemplate.delete(CACHE_BLOG_KEY + id);
        return Result.ok();
    }

    @Override
    public Result queryBlogLikes(Long id) {
        Long userId = UserHolder.getUser().getId();
        Double score = stringRedisTemplate.opsForZSet().score(BLOG_LIKED_KEY + id, userId.toString());
        //1.判断当前用户是否已经点赞
        if (score == null){
            //未点赞，数据库修改加1
            boolean isSuccess = update().setSql("liked = liked + 1").eq("id", id).update();
            if (isSuccess){
                //2.保存点赞列表到redis
                stringRedisTemplate.opsForZSet().add(BLOG_LIKED_KEY + id, userId.toString(), System.currentTimeMillis());
            }
        } else {
            //3.已点赞，取消点赞  数据库减1
            boolean isSuccess = update().setSql("liked = liked - 1").eq("id", id).update();
            //4.从redis 中移除点赞用户
            if (isSuccess){
                stringRedisTemplate.opsForZSet().remove(BLOG_LIKED_KEY + id, userId.toString());
            }
        }
        return Result.ok();
    }
}
