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
        //1.请求并携带cookie
        HttpSession session = request.getSession();
        //2.session中获取用户
        Object user = session.getAttribute("user");
        //3.判断用户是否存在
        if (user == null){
            response.setStatus(401);
            return false;
        }
        //4.保存用户到ThreadLocal
        UserHolder.saveUser((UserDTO) user);
        //放行
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //退出登录
        UserHolder.removeUser();
    }
}
