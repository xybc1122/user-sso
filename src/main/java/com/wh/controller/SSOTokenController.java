package com.wh.controller;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.wh.base.JsonData;
import com.wh.base.ResponseBase;
import com.wh.entity.user.UserInfo;
import com.wh.service.redis.RedisService;
import com.wh.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/sso")
public class SSOTokenController {

    @Autowired
    private RedisService redisService;
    @Autowired
    private UserService userService;

    /**
     * 登陆
     *
     * @param user
     * @return
     */
    @PostMapping("/login")
    @HystrixCommand(fallbackMethod = "loginHystrix")
    public ResponseBase login(HttpServletRequest request, @Valid @RequestBody UserInfo user, BindingResult bindingResult) {
        return userService.doGetAuthenticationInfo(request, user, bindingResult);
    }

    //注意，方法签名一定要要和api方法一致  熔断
    private ResponseBase loginHystrix(HttpServletRequest request, UserInfo user, BindingResult bindingResult) {

        System.out.println("这里可以配置 redis 发送短信 异常报警");

        return JsonData.setResultError("服务调用服务接口失败/延迟,请重试");
    }

    /**
     * 退出
     *
     * @return
     */
    @GetMapping("/logout")
    public ResponseBase logout(@RequestParam("uid") String uid, @RequestParam("tenant") String tenant) {
        String cookieRedisKey = RedisService.redisTokenKey(uid, tenant);
        System.out.println("退出");
        //删除redis
        redisService.delKey(cookieRedisKey);
        //删除cookie
        // SsoLoginStore.removeTokenByCookie(request, response, Constants.SSO_TOKEN);
        return JsonData.setResultSuccess("注销成功!");
    }


}
