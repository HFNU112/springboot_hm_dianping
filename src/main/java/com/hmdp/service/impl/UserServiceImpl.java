package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.*;
import org.aspectj.weaver.ast.Var;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private IUserService userService;

    @Override
    public Result sendCode(String phone, HttpSession session) {
        // 1.校验手机号
        if (RegexUtils.isPhoneInvalid(phone)){
            return Result.fail("手机号不合法~请重新输入");
        }
        // 2.合法，生成验证码
        String code = RandomUtil.randomNumbers(6);
        // 3.保存到session
        // 3.保存到redis
//        session.setAttribute("code",code);
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);
        // 4.发送验证码 接入oss阿里云短信服务--->模拟短信
        log.debug("【黑马点评】尊敬的用户，您正在进行手机号码登录操作。验证码是:【" + code +"】。如非本人操作，还请忽略。五分钟内有效");
        return Result.ok(code);
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        String phone = loginForm.getPhone();
        //1.校验手机号和验证码
        if (RegexUtils.isPhoneInvalid(phone)){
            return Result.fail("手机号格式输入有误，请重新输入！");
        }
        String code = loginForm.getCode();
//        Object cacheCode = session.getAttribute("code");
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        // 验证码不一致
        if (cacheCode == null || !cacheCode.toString().equals(code)){
            return Result.fail("验证码有误！");
        }
        //2.根据手机号查询用户  select * from tb_user where phone = ?
        User user = query().eq("phone", phone).one();
        //3.判断用户是否存在
        if (user == null){
            //不存在, 创建新用户
            user = createUserWithPhone(phone);
        }
        //存在，用户信息保存到session
        //用户数据保存到redis
//        session.setAttribute("user", BeanUtil.copyProperties(user, UserDTO.class));
        String token = UUID.randomUUID().toString();
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));

        stringRedisTemplate.opsForHash().putAll(LOGIN_USER_KEY + token, userMap);
        stringRedisTemplate.expire(LOGIN_USER_KEY + token, LOGIN_USER_TTL, TimeUnit.MINUTES);
        return Result.ok(token);
    }

    /**
     * 用户当天签到
     */
    @Override
    public Result sign() {
        //获取当前用户
        Long userId = UserHolder.getUser().getId();

        //获取当前日期
        LocalDateTime current = LocalDateTime.now();
        String format = current.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        //获取今天是本月的第几天
        int dayOfMonth = current.getDayOfMonth();

        //写入redis
        stringRedisTemplate.opsForValue().setBit(RedisConstants.USER_SIGN_KEY + userId + ":" + format, dayOfMonth - 1, true);
        stringRedisTemplate.expire(RedisConstants.USER_SIGN_KEY + userId + ":" + format, RedisConstants.USER_SIGN_TTL, TimeUnit.DAYS);
        return Result.ok("签到成功");
    }

    @Override
    public Result queryUserById(Long userId) {
        User user = userService.getById(userId);
        if (user == null){
            return Result.ok();
        }
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        return Result.ok(userDTO);
    }

    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName(SystemConstants.USER_NICK_NAME_PREFIX + RandomUtil.randomString(8));
        //保存用户
        save(user);
        return user;
    }


}
