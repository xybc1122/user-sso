package com.wh.service.user.impl;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wh.base.JsonData;
import com.wh.base.ResponseBase;
import com.wh.dto.UserDto;
import com.wh.entity.role.WhUserRole;
import com.wh.entity.user.UserInfo;
import com.wh.exception.LsException;
import com.wh.mapper.UserMapper;
import com.wh.service.redis.RedisService;
import com.wh.service.role.IWhUserRoleService;
import com.wh.service.user.UserService;
import com.wh.toos.Constants;
import com.wh.utils.*;
import ma.glasnost.orika.MapperFacade;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserInfo> implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private IWhUserRoleService roleService;

    /**
     * dto 转换工具
     */
    @Autowired
    private MapperFacade mapperFacade;

    @Autowired
    private RedisService redisService;

    /**
     * 用户认证
     *
     * @param response
     * @return
     */
    @Override
    public ResponseBase doGetAuthenticationInfo(HttpServletRequest request, HttpServletResponse response, UserInfo userInfo) {
        if (StringUtils.isBlank(userInfo.getUserName()) || StringUtils.isBlank(userInfo.getPwd())) {
            return JsonData.setResultError("账号/或密码不能为空");
        }
        String ttlDateKey = Constants.TTL_DATE + userInfo.getUserName();
        Long ttlDate = redisService.getTtl(ttlDateKey);
        //如果不等于null
        if (ttlDate != -1 && ttlDate != -2) {
            return JsonData.setResultError("账号/或密码错误被锁定/" + ttlDate + "秒后到期!");
        }
        String md5Pwd = MD5Util.saltMd5(userInfo.getUserName(), userInfo.getPwd());
        LambdaQueryWrapper<UserInfo> lambdaQuery;
        //查询用户信息 更新更新登陆时间
        lambdaQuery = WrapperUtils.getLambdaQuery();
        lambdaQuery.eq(UserInfo::getUserName, userInfo.getUserName()).eq(UserInfo::getPwd, md5Pwd);
        UserInfo user = userMapper.selectOne(lambdaQuery);
        try {
            // 账号不存在 异常
            if (user == null) {
                return JsonData.setResultError("未知账户/没找到帐号,登录失败");
            }
            if (user.getAccountStatus() == 1) {
                return JsonData.setResultError("账号已被锁定,请联系管理员");
            }
            if (user.getDelOrNot() == 1) {
                return JsonData.setResultError("账号凭着已过期/或删除 请联系管理员");
            }
            //查询角色 配置角色装进token
            WhUserRole whUserRole = roleService.serviceSelRids(user.getUid());
            if (whUserRole != null) {
                user.setRids(whUserRole.getrIds());
            }
            //更新登陆时间
            lambdaQuery = WrapperUtils.getLambdaQuery();
            int result = userMapper.update(new UserInfo(new Date().getTime()),
                    lambdaQuery.eq(UserInfo::getUid, user.getUid()));
            JsonUtils.saveResult(result);
            //设置token  Cookie
            JSONObject uJson = put(response, user, userInfo.isRememberMe());
            //登陆成功后 删除Redis指定数据
            redisService.delKey(RedisUtils.redisErrorKey(user.getUserName()));
            return JsonData.setResultSuccess(uJson);
        } catch (LsException ls) {
            return setLockingTime(userInfo.getUserName());
        }
    }

    private JSONObject put(HttpServletResponse response, UserInfo user, boolean ifRemember) {
        long time;
        if (ifRemember) {
            time = 60 * 60 * 24 * 7L;
        } else {
            time = 30 * 60L;
        }
        //设置 JwtToken
        String token = JwtUtils.genJsonWebToken(user);
        //转换dto层
        UserDto userDto = mapperFacade.map(user, UserDto.class);

        JSONObject uJson = new JSONObject();
        uJson.put("user", userDto);
        uJson.put("token", token);

        //设置token
        redisService.setString(RedisUtils.redisTokenKey(userDto.getUid()), token, time);

        //设置Cookie
        CookieUtil.set(response, Constants.SSO_TOKEN, token, ifRemember);

        return uJson;
    }

    private ResponseBase setLockingTime(String userName) {
        int errorNumber = 0;
        errorNumber++;
        int errorFre;
        long lockingTime;
        String errKey = RedisUtils.redisErrorKey(userName);
        String redisErrorNumber = redisService.getStringKey(errKey);
        //报错后 先进来看看 这个账号有没有在Redis里 ---如果里面有 进去
        if (redisErrorNumber != null) {
            errorFre = (Integer.parseInt(redisErrorNumber) + errorNumber);
            redisService.setString(errKey, Integer.toString(errorFre));
        } else {
            //如果是null  只会走这里
            redisService.setString(errKey, Integer.toString(errorNumber));
            return JsonData.setResultError("账号或密码错误/你还有" + (4 - errorNumber + "次机会"));
        }
        if (errorFre >= 4) {
            switch (errorFre) {
                case 4:
                    lockingTime = (long) 5;
                    break;
                case 5:
                    lockingTime = 5L * 5;
                    break;
                case 6:
                    lockingTime = 10L * 5;
                    break;
                case 7:
                    lockingTime = 15L * 5;
                    break;
                default:
                    lockingTime = 60L * 60 * 24;
            }
            redisService.setString(Constants.TTL_DATE + userName, "error", lockingTime);
            return JsonData.setResultError("账号被锁定!" + lockingTime + "秒");
        }
        return JsonData.setResultError("账号或密码错误/你还有" + (4 - errorFre + "次机会"));
    }

//    /**
//     * web 用户认证
//     *
//     * @param response
//     * @return
//     */
//    @Override
//    public void doGetAuthenticationInfo(HttpServletRequest request,
//                                        HttpServletResponse response, UserInfo user) {
//        String redirectUrl = request.getParameter(Constants.REDIRECT_URL);
//
//        if (StringUtils.isBlank(redirectUrl) && redirectUrl.trim().length() > 0) {
//            JsonUtils.sendJsonMsg(response,
//                    JsonData.setResultError(Constants.REDIRECT_URL + "is Null "));
//            return;
//        }
//        String ttlDateKey = Constants.TTL_DATE + user.getUserName();
//        Long ttlDate = redisService.getTtl(ttlDateKey);
//        //如果不等于null
//        if (ttlDate != -1 && ttlDate != -2) {
//            JsonUtils.sendJsonMsg(response,
//                    JsonData.setResultError("账号/或密码错误被锁定/" + ttlDate + "秒后到期!"));
//            return;
//        }
//        LambdaQueryWrapper<UserInfo> lambdaQuery;
//        //查询用户信息 更新更新登陆时间
//        lambdaQuery = WrapperUtils.getLambdaQuery();
//        lambdaQuery.eq(UserInfo::getUserName, user.getUserName());
//        UserInfo dbUser = userMapper.selectOne(lambdaQuery);
//        try {
//            // 账号不存在 异常
//            if (dbUser == null) {
//                JsonUtils.sendJsonMsg(response,
//                        JsonData.setResultError("未知账户/没找到帐号,登录失败"));
//                return;
//            }
//            if (dbUser.getAccountStatus() == 1) {
//                JsonUtils.sendJsonMsg(response,
//                        JsonData.setResultError("账号已被锁定,请联系管理员"));
//                return;
//            }
//            if (dbUser.getDelOrNot() == 1) {
//                JsonUtils.sendJsonMsg(response,
//                        JsonData.setResultError("账号凭着已过期/或删除 请联系管理员"));
//                return;
//            }
//            //查询角色 配置角色装进token
//            WhUserRole whUserRole = roleService.serviceSelRids(user.getUid());
//            if (whUserRole != null) {
//                user.setRids(whUserRole.getrIds());
//            }
//            //更新登陆时间
//            UserInfo upUser = new UserInfo();
//            upUser.setLandingTime(new Date().getTime());
//            lambdaQuery = WrapperUtils.getLambdaQuery();
//            lambdaQuery.eq(UserInfo::getUid, user.getUid());
//            int result = userMapper.update(upUser, lambdaQuery);
//            JsonUtils.saveResult(result);
//
//            //设置token  Cookie
//            String token = put(response, dbUser, user.isRememberMe());
//
//            //登陆成功后 删除Redis指定数据
//            redisService.delKey(RedisUtils.redisErrorKey(user.getUserName()));
//            //这里登陆成功了
//            String redirectUrlFinal = redirectUrl + "?" + Constants.TOKEN + "=" + token;
//            //这里重定向回去
//            response.sendRedirect(redirectUrlFinal);
//        } catch (LsException ls) {
//            setLockingTime(response, user.getUserName());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private String put(HttpServletResponse response, UserInfo user, boolean ifRemember) {
//        long time;
//        if (ifRemember) {
//            time = 60 * 60 * 24 * 7L;
//        } else {
//            time = 30 * 60L;
//        }
//        //设置 JwtToken
//        String token = JwtUtils.genJsonWebToken(user);
//        if (token == null) throw new NullPointerException("--设置token失败");
//        //转换dto层
//        UserDto userDto = mapperFacade.map(user, UserDto.class);
//        String sessionId = SsoSessionIdHelper.makeSessionId(userDto.getUid());
//
//        //设置token
//        redisService.setString(RedisUtils.redisTokenKey(sessionId), token, time);
//
//        //设置Cookie
//        CookieUtil.set(response, Constants.TOKEN, token, ifRemember);
//
//        return token;
//    }
//
//    private void setLockingTime(HttpServletResponse response, String userName) {
//        int errorNumber = 0;
//        errorNumber++;
//        int errorFre;
//        long lockingTime;
//        String errKey = RedisUtils.redisErrorKey(userName);
//        String redisErrorNumber = redisService.getStringKey(errKey);
//        //报错后 先进来看看 这个账号有没有在Redis里 ---如果里面有 进去
//        if (redisErrorNumber != null) {
//            errorFre = (Integer.parseInt(redisErrorNumber) + errorNumber);
//            redisService.setString(errKey, Integer.toString(errorFre));
//        } else {
//            //如果是null  只会走这里
//            redisService.setString(errKey, Integer.toString(errorNumber));
//            JsonUtils.sendJsonMsg(response,
//                    JsonData.setResultError("账号或密码错误/你还有" + (4 - errorNumber + "次机会")));
//            return;
//        }
//        if (errorFre >= 4) {
//            switch (errorFre) {
//                case 4:
//                    lockingTime = (long) 5;
//                    break;
//                case 5:
//                    lockingTime = 5L * 5;
//                    break;
//                case 6:
//                    lockingTime = 10L * 5;
//                    break;
//                case 7:
//                    lockingTime = 15L * 5;
//                    break;
//                default:
//                    lockingTime = 60L * 60 * 24;
//            }
//            redisService.setString(Constants.TTL_DATE + userName, "error", lockingTime);
//            JsonUtils.sendJsonMsg(response,
//                    JsonData.setResultError("账号被锁定!" + lockingTime + "秒"));
//            return;
//        }
//        JsonUtils.sendJsonMsg(response,
//                JsonData.setResultError("账号或密码错误/你还有" + (4 - errorFre + "次机会")));
//    }
}
