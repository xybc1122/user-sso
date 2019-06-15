package com.wh.controller;

import com.wh.base.JsonData;
import com.wh.base.ResponseBase;
import com.wh.entity.user.UserInfo;
import com.wh.service.redis.RedisService;
import com.wh.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.wh.toos.Constants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
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
    @ResponseBody
    @GetMapping("/verify")
    public String verifyToken(@RequestParam("token") String token, @RequestParam("uid") String uid) {
        String redisToken = redisService.getStringKey(Constants.TOKEN + ":" + uid);
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
    @ResponseBody
    @PostMapping("/ajaxLogin")
    public ResponseBase login(HttpServletResponse response, HttpServletRequest request, @RequestBody UserInfo user) {
        String ttlDateKey = Constants.TTL_DATE + user.getUserName();
        Long ttlDate = redisService.getTtl(ttlDateKey);
        //如果不等于null
        if (ttlDate != -1 && ttlDate != -2) {
            return JsonData.setResultError("账号/或密码错误被锁定/" + ttlDate + "秒后到期!");
        }
        return userService.doGetAuthenticationInfo(request, response, user);
    }


//    /**
//     * 退出
//     *
//     * @return
//     */
//    @GetMapping("/logout")
//    public ResponseBase logout(HttpServletRequest request, HttpServletResponse response) {
//        //删除redis token
//        Boolean result = redisService.delKey(Constants.TOKEN + ":" + ReqUtils.getUid());
//        //删除 cookie里的  token
//        SsoLoginStore.removeTokenByCookie(request, response);
//        //删除webSocket
//        ChannelHandlerContext ctx = chatService.getCtx(ReqUtils.getUid());
//        if (ctx != null) {
//            ctx.channel().close();
//        }
//        if (result) {
//            return JsonData.setResultSuccess("注销成功!");
//        }
//        throw new LsException("注销失败");
//    }
}
