package com.wh.store;


import com.wh.entity.user.UserInfo;
import com.wh.exception.LsException;
import com.wh.toos.Constants;
import com.wh.utils.CookieUtil;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * local ssologin com.wh.store
 */
@Component
public class SsoLoginStore {
    /**
     * 删除 token,Cookie
     *
     * @param request
     * @param response
     */
    public static void removeTokenByCookie(HttpServletRequest request, HttpServletResponse response) {
        CookieUtil.remove(request, response, Constants.TOKEN);
    }

    /**
     * client ssologin
     */
    public static void login(UserInfo user, String pwd) {
        if (!user.getPwd().equals(pwd)) {
            throw new LsException("密码错误");
        }
        user.setPwd(null);
        user.setLandingTime(new Date().getTime());
    }

}
