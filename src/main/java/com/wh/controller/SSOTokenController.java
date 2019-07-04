package com.wh.controller;

import com.wh.base.JsonData;
import com.wh.base.ResponseBase;
import com.wh.entity.user.UserInfo;
import com.wh.utils.RedisUtils;
import com.wh.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/v1/sso")
public class SSOTokenController {

    @Autowired
    private RedisUtils redisService;
    @Autowired
    private UserService userService;

    /**
     * 登陆
     *
     * @param user
     * @return
     */
    @PostMapping("/login")
    public ResponseBase login(HttpServletResponse response, HttpServletRequest request, @RequestBody UserInfo user) {
        return userService.doGetAuthenticationInfo(request, response, user);
    }

    /**
     * 退出
     *
     * @return
     */
    @GetMapping("/logout")
    public ResponseBase logout(@RequestParam("uid") String uid, @RequestParam("tenant") String tenant) {
        String cookieRedisKey = RedisUtils.redisTokenKey(uid, tenant);
        System.out.println("退出");
        //删除redis
        redisService.delKey(cookieRedisKey);
        //删除cookie
        // SsoLoginStore.removeTokenByCookie(request, response, Constants.SSO_TOKEN);
        return JsonData.setResultSuccess("注销成功!");
    }




}
