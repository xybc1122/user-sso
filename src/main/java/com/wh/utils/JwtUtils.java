package com.wh.utils;

import com.wh.entity.user.UserInfo;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * jwt工具类
 */
public class JwtUtils {

    public static final String SUBJECT = "userToken";


    public static final String APPSECRET = "boot999";


    /**
     * 生成jwt token
     *
     * @param user
     * @return
     */
    public static String genJsonWebToken(UserInfo user) {
        if (user == null || user.getUid() == null || StringUtils.isBlank(user.getUserName())) {
            throw new NullPointerException("--设置token失败");
        }
        return Jwts.builder().setSubject(SUBJECT)
                .claim("uid", user.getUid())
                .claim("userName", user.getUserName())
                .claim("tenant", user.getTenant())
                .claim("rIds", user.getRids())
                .setIssuedAt(new Date())//设置新的时间
//                .setExpiration(new Date(system.currentTimeMillis() + EXPIRE))//过期时间
                .signWith(SignatureAlgorithm.HS256, APPSECRET).compact();
    }
}