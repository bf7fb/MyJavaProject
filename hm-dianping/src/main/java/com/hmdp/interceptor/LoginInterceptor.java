package com.hmdp.interceptor;

import com.hmdp.utils.UserHolder;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author wz
 * @ClassName LoginInterceptor
 * @date 2023/2/20 12:33
 * @Description TODO
 */
public class LoginInterceptor implements HandlerInterceptor {


//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
////        // 1.获取session
////        HttpSession session = request.getSession();
////        // 2.获取session中用户
////        UserDTO user = (UserDTO) session.getAttribute("user");
////        // 3.判断用户是否存在
////        if (user == null) {
////            // 4.不存在拦截
////            response.setStatus(401);
////            return false;
////        }
////        // 5.存在 放入treadlocal变量中
////        UserHolder.saveUser(user);
//        // 1.从请求头中获取token
//        String token = request.getHeader("authorization");
//        // 2.如果token为空 此时请求为登录 未授权
//        if (StringUtils.isBlank(token)) {
//            response.setStatus(401);
//            return false;
//        }
//        // 3.根据token获取user 为空则代表未登录
//        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(LOGIN_USER_KEY + token);
//        if (userMap.isEmpty()) {
//            response.setStatus(401);
//            return false;
//        }
//        // 4.将查询的map对象重新转换为dto
//        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
//        // 5.将userDto保存至threadlocal中
//        UserHolder.saveUser(userDTO);
//        // 6.刷新tokenKey的有效期
//        stringRedisTemplate.expire(LOGIN_USER_KEY + token,LOGIN_USER_TTL, TimeUnit.MINUTES);
//        // 7.放行
//        return true;
//    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1.判断是否需要拦截（ThreadLocal中是否有用户）
        if (UserHolder.getUser() == null) {
            // 没有，需要拦截，设置状态码
            response.setStatus(401);
            // 拦截
            return false;
        }
        // 有用户，则放行
        return true;
    }


}
