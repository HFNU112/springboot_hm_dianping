package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import com.hmdp.utils.SystemConstants;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

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

    @Override
    public Result sendCode(String phone, HttpSession session) {
        // 1.校验手机号
        if (RegexUtils.isPhoneInvalid(phone)){
            return Result.fail("手机号不合法~请重新输入");
        }
        // 2.合法，生成验证码
        String code = RandomUtil.randomNumbers(6);
        // 3.保存到session
        session.setAttribute("code",code);
        // 4.发送验证码 接入oss阿里云短信服务--->模拟短信
        log.debug("短信发送成功，您的验证码是【" + code +"】");
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        String phone = loginForm.getPhone();
        //1.校验手机号和验证码
        if (RegexUtils.isPhoneInvalid(phone)){
            return Result.fail("手机号格式输入有误，请重新输入！");
        }
        String code = loginForm.getCode();
        Object cacheCode = session.getAttribute("code");
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
        session.setAttribute("user", BeanUtil.copyProperties(user, UserDTO.class));
        return Result.ok();
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
