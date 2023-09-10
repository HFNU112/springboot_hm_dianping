package com.hmdp.intercepter;

import com.hmdp.dto.UserDTO;
import com.hmdp.utils.UserHolder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author: Husp
 * @date: 2023/9/10 16:53
 */
public class LoginIntercepter implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断ThreadLocal中是否存在用户
        UserDTO user = UserHolder.getUser();
        if (user == null){
            response.setStatus(401);
            //拦截
            return false;
        }
        //放行
        return true;
    }

}
