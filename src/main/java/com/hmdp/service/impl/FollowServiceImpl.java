package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Follow;
import com.hmdp.mapper.FollowMapper;
import com.hmdp.service.IFollowService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.service.IUserService;
import com.hmdp.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.hmdp.utils.RedisConstants.CACHE_FOLLOW_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private IUserService userService;

    @Override
    public Result follow(Long followUserId, Boolean isFollow) {
        // 获取登录用户
        Long userId = UserHolder.getUser().getId();

        // 判断登录用户是否关注
        if (isFollow){
            //如果状态true, 可以关注 向数据库新增数据
            Follow follow = new Follow();
            follow.setUserId(userId);
            follow.setFollowUserId(followUserId);
            boolean isSuccess = save(follow);
            if (isSuccess){
                //关注的人存入redis
                stringRedisTemplate.opsForSet().add(CACHE_FOLLOW_KEY + userId, followUserId.toString());
            }
        }else {
            //如果状态false, 就取消关注
            boolean isSuccess = remove(new QueryWrapper<Follow>()
                    .eq("user_id", userId)
                    .eq("follow_user_id", followUserId));
            if (isSuccess){
                //从redis中移除关注人列表
                stringRedisTemplate.opsForSet().remove(CACHE_FOLLOW_KEY + userId, followUserId.toString());
            }
        }
        return Result.ok();
    }

    @Override
    public Result isFollow(Long followUserId) {
        //获取登录用户
        Long userId = UserHolder.getUser().getId();

        // 判断是否关注
        Integer count = query().eq("user_id", userId).eq("follow_user_id", followUserId).count();

        //查询统计数大于0 表示已关注
        return Result.ok(count > 0);
    }

    @Override
    public Result followCommon(Long followUserId) {
        //获取当前登录人
        Long userId = UserHolder.getUser().getId();

        //从redis求登录人和关注人的交集
        Set<String> intersect = stringRedisTemplate.opsForSet()
                .intersect(CACHE_FOLLOW_KEY + userId, CACHE_FOLLOW_KEY + followUserId);
        //判断交集是否为空
        if (intersect == null || intersect.isEmpty()){
            //如果为空，直接返回
            return Result.ok(Collections.emptyList());
        }
        //不为空，查询这些用户集合
        List<Long> followUserIds = intersect.stream().map(Long::valueOf).collect(Collectors.toList());
        List<UserDTO> users = userService.listByIds(followUserIds)
                .stream().map(user -> BeanUtil.copyProperties(user,UserDTO.class))
                .collect(Collectors.toList());
        //返回关注用户集合
        return Result.ok(users);
    }

}
