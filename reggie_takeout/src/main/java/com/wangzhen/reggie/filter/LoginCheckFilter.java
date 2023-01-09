package com.wangzhen.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.wangzhen.reggie.common.BaseContext;
import com.wangzhen.reggie.common.Result;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author wz
 * @ClassName LoginCheckFilter
 * @date 2023/1/5 17:43
 * @Description TODO
 */
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
    // 匹配/backend/xxx ,/front/xxx 和 /backend/** , /front/**
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //1.获取本次请求的uri
        String requestURI = request.getRequestURI();

        //2.定义需要放行的数组
        String[] urls = new String[]{
            "/employee/login",
            "/employee/logout",
            "/backend/**",
            "/front/**",
            "/user/sendMsg",
            "/user/login"
        };

        //3. 做校验
        boolean check = check(urls, requestURI);
        //如果是登录或退出及静态资源请求则放行
        if (check){
            filterChain.doFilter(request,response);
            return;
        }

        //如果已登录 则放行
        if (request.getSession().getAttribute("employee") != null){
            // 登录过后 将employee存放在threadlocal变量中
            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);
            filterChain.doFilter(request,response);
            return;
        }

        if (request.getSession().getAttribute("user") != null){
            // 登录过后 将employee存放在threadlocal变量中
            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);
            filterChain.doFilter(request,response);
            return;
        }

        //4.如果未登录及非法请求 则返回NOTLOGIN  注意：前端实现页面跳转
        response.getWriter().write(JSON.toJSONString(Result.error("NOTLOGIN")));
    }

    public boolean check(String[] urls,String requestUri){
        for (String url : urls) {
            boolean result = PATH_MATCHER.match(url, requestUri);
            if (result){
                return true;
            }
        }
        return false;
    }
}
