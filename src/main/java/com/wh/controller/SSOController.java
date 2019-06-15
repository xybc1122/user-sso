package com.wh.controller;

import com.wh.base.JsonData;
import com.wh.base.ResponseBase;
import com.wh.entity.user.UserInfo;
import com.wh.service.redis.RedisService;
import com.wh.service.user.UserService;
import com.wh.store.SsoLoginStore;
import com.wh.store.SsoSessionIdHelper;
import com.wh.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.wh.toos.Constants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/v1/sso")
public class SSOController {

    @Autowired
    private RedisService redisService;
    @Autowired
    private UserService userService;


    /**
     * token校验
     *
     * @param token
     * @return
     */
    @GetMapping("/verify")
    public String verifyToken(@RequestParam("token") String token, @RequestParam("uid") String uid) {
        String redisToken = redisService.getStringKey
                (RedisUtils.redisTokenKey(uid));
        if (token.equals(redisToken)) {
            return "true";
        }
        return "false";
    }

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
    public ResponseBase logout(HttpServletRequest request, HttpServletResponse response, @RequestParam("uid") String uid) {
        String cookieRedisKey = RedisUtils.redisTokenKey(uid);
        //删除redis
        redisService.delKey(cookieRedisKey);
        //删除cookie
        SsoLoginStore.removeTokenByCookie(request, response, Constants.SSO_TOKEN);
        return JsonData.setResultSuccess("注销成功!");
    }
//    /**
//     *  web Login
//     *
//     * @param request
//     * @return
//     */
//    @PostMapping("/doLogin")
//    public void doLogin(HttpServletRequest request,
//                        HttpServletResponse response, @RequestBody UserInfo user) {
//        userService.doGetAuthenticationInfo(request, response, user);
//    }


//    /**
//     * Logout
//     *
//     * @param request
//     * @return
//     */
//    @GetMapping("/logout")
//    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        String cookieRedisKey = SsoLoginStore.getCookieRedisKey(request);
//        //删除redis
//        redisService.delKey(cookieRedisKey);
//        //删除cookie
//        SsoLoginStore.removeTokenByCookie(request, response, Constants.TOKEN);
//        //重定向回登陆页面
//        response.sendRedirect("http://127.0.0.1:8080/login" + "?" + Constants.REDIRECT_URL + "=" + request.getParameter(Constants.REDIRECT_URL));
//    }


}
