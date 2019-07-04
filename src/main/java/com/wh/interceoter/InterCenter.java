package com.wh.interceoter;


import com.wh.base.ApplicationContextRegister;
import com.wh.base.JsonData;
import com.wh.dds.DynamicDataSourceContextHolder;
import com.wh.utils.IpUtils;
import com.wh.utils.JsonUtils;
import com.wh.utils.RedisUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 监听器
 */
public class InterCenter implements HandlerInterceptor {


    /**
     * redis
     */
    private RedisUtils redisService = ApplicationContextRegister.getBean(RedisUtils.class);

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
        String ip = IpUtils.getIpAddr(request);
        //这里判断频繁请求 api  限制
        if (!accessLimit(request, response, ip)) return false;

        return true;
    }

    private boolean accessLimit(HttpServletRequest request, HttpServletResponse response, String id) {
        // seconds是多少秒内可以访问多少次
        long seconds = 10;
        //5次
        int maxCount = 5;
        String key = request.getRequestURI();
        String tKey = key + "_" + id;
        //从redis中获取用户访问的次数
        String count = redisService.getStringKey(tKey);
        if (count == null) {
            //第一次访问
            redisService.setString(tKey, "1", seconds);
        } else if (Integer.parseInt(count) < maxCount) {
            //加1
            redisService.setEx(tKey, 1);
        } else {
            //超出访问次数
            JsonUtils.sendJsonMsg(response, JsonData.setResultError(-1, "访问太频繁,请稍后在试"));
            return false;
        }
        return true;
    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        //System.out.println("postHandle");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //System.out.println("afterCompletion");
    }

}
