package com.wh.Intercepter;


import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 监听器
 */
public class LoginInterCenter implements HandlerInterceptor {
    //回调登陆的url地址
    private final static String LOG_URL = "http://127.0.0.1:8081/login#/login";

    /**
     * 用户登录进入controller层之前 进行拦截
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String url = request.getRequestURI();
        if (url.equals("/checkLogin")) {
            String redirectUrl = request.getParameter("redirectUrl");
            //这里直接重定向到登陆页面
            response.sendRedirect(LOG_URL + "?redirectUrl=" + redirectUrl);
            return false;
        } else if (url.equals("verify")) {
            return StringUtils.isNotBlank(request.getParameter("token")) || StringUtils.isNotBlank(request.getParameter("uid"));
        }
        return false;
    }

}
