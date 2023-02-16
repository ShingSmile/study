package com.study.filter;

import com.alibaba.fastjson.JSON;
import com.study.common.BaseContext;
import com.study.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;
import java.io.IOException;

@Slf4j
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
public class LoginCheckFilter implements Filter {

    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //获取本次请求的URI
        String requestURI = request.getRequestURI();
        //将拦截到的URI输出到日志，{}是占位符，将自动填充request.getRequestURI()的内容
        log.info("拦截到的URI：{}", request.getRequestURI());

        //定义不需要被拦截的请求
        String[] urls = new String[]{
                "/employee/login",
                "/employee/login.html",
                "/employee/logout.html",
                "/backend/**",
                "/front/**",
                "/common/**",

                "/user/login",
                "/user/sendMsg"
        };


        //2.判断本次请求是否需要处理
        boolean check = check(urls, requestURI);

        //3.如果不需要处理，则直接放行
        if (check) {
            log.info("本次请求：{}，不需要处理",requestURI);
            filterChain.doFilter(request,response);
            return;
        }

        //4.判断登录状态，如果已登录，则直接放行
        if (request.getSession().getAttribute("employee") != null) {
            log.info("用户已登录，id为{}",request.getSession().getAttribute("employee"));
            long id = Thread.currentThread().getId();
            log.info("doFilter的线程id为：{}", id);
            //根据session来获取之前我们存的id值
            Long empId = (Long) request.getSession().getAttribute("employee");
            //使用BaseContext封装id
            BaseContext.setCurrentId(empId);
            filterChain.doFilter(request,response);
            return;
        }

        //判断用户是否登录
        if(request.getSession().getAttribute("user") != null){
            log.info("用户已登录，用户id为：{}",request.getSession().getAttribute("user"));
            Long userId = (Long)request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);
            filterChain.doFilter(request,response);
            return;
        }

        //5.如果未登录则返回未登录结果,通过输出流方式向客户端页面响应数据
        log.info("用户未登录");
        log.info("用户id{}",request.getSession().getAttribute("employee"));
        Result res = Result.error("NOTLOGIN");
        response.getWriter().write(JSON.toJSONString(res));

    }

    private boolean check(String[] urls, String uri) {
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, uri);
            if (match)
                return true;
        }
        return false;
    }
}
