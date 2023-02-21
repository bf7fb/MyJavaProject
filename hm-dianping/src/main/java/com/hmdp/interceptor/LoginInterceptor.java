package com.hmdp.interceptor;

import com.hmdp.dto.UserDTO;
import com.hmdp.utils.UserHolder;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author wz
 * @ClassName LoginInterceptor
 * @date 2023/2/20 12:33
 * @Description TODO
 */
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1.获取session
        HttpSession session = request.getSession();
        // 2.获取session中用户
        UserDTO user = (UserDTO) session.getAttribute("user");
        // 3.判断用户是否存在
        if (user == null) {
            // 4.不存在拦截
            response.setStatus(401);
            return false;
        }
        // 5.存在 放入treadlocal变量中
        UserHolder.saveUser(user);
        // 6.放行
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}
