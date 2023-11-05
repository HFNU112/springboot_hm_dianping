package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.dto.ScrollResult;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Blog;
import com.hmdp.entity.Follow;
import com.hmdp.entity.User;
import com.hmdp.mapper.BlogMapper;
import com.hmdp.service.IBlogService;
import com.hmdp.service.IFollowService;
import com.hmdp.service.IUserService;
import com.hmdp.utils.SystemConstants;
import com.hmdp.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.hmdp.utils.RedisConstants.*;
import static com.hmdp.utils.SystemConstants.MAX_PAGE_SIZE;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private IUserService userService;

    @Resource
    private IFollowService followService;

    @Override
    public Result saveBlog(Blog blog) {
        // 获取登录用户
        UserDTO user = UserHolder.getUser();
        blog.setUserId(user.getId());
        // 保存探店博文
        boolean isSuccess = save(blog);
        if (!isSuccess) {
            return Result.fail("新增笔记失败");
        }
        //查询笔记作者的所有粉丝 SELECT * FROM tb_follow WHERE follow_user_id = ?
        List<Follow> follows = followService.query().eq("follow_user_id", user.getId()).list();
        for (Follow follow : follows) {
            //获取粉丝id
            Long userId = follow.getUserId();
            //推送所有笔记的粉丝id
            stringRedisTemplate.opsForZSet().add(CACHE_FEED_KEY + userId, blog.getId().toString(), System.currentTimeMillis());
        }
        //返回粉丝id
        return Result.ok(blog.getId());
    }

    @Override
    public Result queryBlogById(Long id) {
        //1.从redis中查询Blog
//        String blogJson = stringRedisTemplate.opsForValue().get(CACHE_BLOG_KEY + id);

        //2.判断缓存key是否命中
//        if (StrUtil.isNotBlank(blogJson)){
//            //命中，直接返回
//            Blog blog = JSONUtil.toBean(blogJson, Blog.class);
//            return Result.ok(blog);
//        }
        //3.未命中，查询数据库Blog表
        Blog blog = getById(id);
        //4.判断数据库查询是否存在
        if (blog == null) {
            //不存在直接返回404
            return Result.fail("当前尚未发布笔记");
        }
        //5.数据库存在并写入redis
//        stringRedisTemplate.opsForValue().set(CACHE_BLOG_KEY + id, JSON.toJSONString(blog), CACHE_BLOG_TTL, TimeUnit.MINUTES);
        // 查询笔记的有关用户
        queryBlogUser(blog);
        //查询笔记是否点过赞
        isBlogLiked(blog);

        //8.返回Blog
        return Result.ok(blog);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result updateLikeBlog(Long id) {
        Long userId = UserHolder.getUser().getId();
        Double score = stringRedisTemplate.opsForZSet().score(BLOG_LIKED_KEY + id, userId.toString());
        // 1.判断当前登录用户是否点赞
        if (score == null) {
            // 2.未点赞，可以点赞，数据库加 1  liked = liked + 1
            boolean isSuccess = update().setSql("liked = liked + 1").eq("id", id).update();
            if (isSuccess) {
                // 3.把点赞信息存到redis
                stringRedisTemplate.opsForZSet().add(BLOG_LIKED_KEY + id, userId.toString(), System.currentTimeMillis());
            }
        } else {
            // 4.已点赞则取消点赞  数据库减 1  liked = liked - 1
            boolean isSuccess = update().setSql("liked = liked - 1").eq("id", id).update();
            if (isSuccess) {
                // 5.从redis 移除点赞信息
                stringRedisTemplate.opsForZSet().remove(BLOG_LIKED_KEY + id, userId.toString());
            }
        }
        return Result.ok();
    }

    @Override
    public Result queryBlogLikes(Long id) {
        //1.从redis中查询前5个点赞用户
        Set<String> top5User = stringRedisTemplate.opsForZSet().range(BLOG_LIKED_KEY + id, 0, 4);
        //判断用户集合
        if (top5User == null || top5User.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }

        //2.获取5个用户id集合
        List<Long> ids = top5User.stream().map(Long::valueOf).collect(Collectors.toList());
        String idStr = StrUtil.join(",", ids);
        //3.根据用户id查询点赞的用户列表
        List<UserDTO> userDTOS = userService
                .query()
                .in("id", ids).last("ORDER BY FIELD(id," + idStr + ")").list()
                .stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .collect(Collectors.toList());
        // 返回用户
        return Result.ok(userDTOS);
    }

    @Override
    public Result queryHotBlog(Integer current) {
        Page<Blog> page = query()
                .orderByDesc("liked")
                .page(new Page<>(current, MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        // 查询用户
        records.forEach(blog -> {
            this.queryBlogUser(blog);
            this.isBlogLiked(blog);
        });
        //返回Blog集合
        return Result.ok(records);
    }

    @Override
    public Result queryBlogByUserId(Long userId, Integer current) {
        Page<Blog> blogPage = query()
                .eq("user_id", userId)
                .page(new Page<Blog>(current, MAX_PAGE_SIZE));
        List<Blog> records = blogPage.getRecords();
        return Result.ok(records);
    }

    @Override
    public Result queryBlogOfFollow(Long max, Integer offset) {
        //获取登录用户
        Long userId = UserHolder.getUser().getId();

        // 从redis查询收件箱
        Set<ZSetOperations.TypedTuple<String>> typedTuples = stringRedisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(CACHE_FEED_KEY + userId, 0, max, offset, 3);

        //判断是否查询到
        if (typedTuples == null || typedTuples.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }
        List<Long> ids = new ArrayList<>(typedTuples.size());
        long minTime = 0;
        int os = 1;
        //解析笔记id、时间戳、偏移量
        for (ZSetOperations.TypedTuple<String> typedTuple : typedTuples) {
            // 获取id
            ids.add(Long.valueOf(typedTuple.getValue()));
            //获取分数 时间戳
            long time = typedTuple.getScore().longValue();
            if (time == minTime) {
                os++;
            } else {
                minTime = time;
                os = 1;
            }
        }
        os = minTime == max ? os : os + offset;
        String idStr = StrUtil.join(",", ids);
        //根据id查询Blog
        List<Blog> blogs = query().in("id", ids).last("order by field(id," + idStr + ")").list();

        for (Blog blog : blogs) {
            //查询blog有关用户
            queryBlogUser(blog);
            //查询blog是否点赞
            isBlogLiked(blog);
        }

        ScrollResult scrollResult = new ScrollResult();
        scrollResult.setList(blogs);
        scrollResult.setOffset(os);
        scrollResult.setMinTime(minTime);
        //返回收件箱
        return Result.ok(scrollResult);
    }

    private void queryBlogUser(Blog blog) {
        Long userId = blog.getUserId();
        User user = userService.getById(userId);
        blog.setIcon(user.getIcon());
        blog.setName(user.getNickName());
    }

    private void isBlogLiked(Blog blog) {
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return;
        }
        Long userId = user.getId();
        Double score = stringRedisTemplate.opsForZSet().score(BLOG_LIKED_KEY + blog.getId(), userId.toString());
        blog.setIsLike(score != null);
    }
}
