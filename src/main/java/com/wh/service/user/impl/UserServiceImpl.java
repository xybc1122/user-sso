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
import com.wh.store.SsoLoginStore;
import com.wh.toos.Constants;
import com.wh.utils.*;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public ResponseBase doGetAuthenticationInfo(HttpServletRequest request, HttpServletResponse response, UserInfo userInfo) {
        LambdaQueryWrapper<UserInfo> lambdaQuery;
        //查询用户信息 更新更新登陆时间
        lambdaQuery = WrapperUtils.getLambdaQuery();
        lambdaQuery.eq(UserInfo::getUserName, userInfo.getUserName());
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
            UserInfo upUser = new UserInfo();
            upUser.setLandingTime(new Date().getTime());
            lambdaQuery = WrapperUtils.getLambdaQuery();
            lambdaQuery.eq(UserInfo::getUid, user.getUid());
            int result = userMapper.update(upUser, lambdaQuery);
            JsonUtils.saveResult(result);

//            chatStore.kickOut(userDto.getUserName() + "token", com.getUid(),
//                    JsonUtils.getJsonTypeError("有人登陆,你被踢下线,若不是本人,请修改密码",
//                            ChatType.KICK_OUT));

            String pwd = MD5Util.saltMd5(user.getUserName(), userInfo.getPwd());
            // 登陆校验
            SsoLoginStore.login(user, pwd);

            //设置token  Cookie
            JSONObject uJson = put(request, response, user, userInfo.isRememberMe());

            //登陆成功后 删除Redis指定数据
            String errKey = Constants.ERROR_LOGIN + user.getUserName();
            redisService.delKey(errKey);
            return JsonData.setResultSuccess(uJson);
        } catch (LsException ls) {
            return setLockingTime(userInfo);
        }
    }

    private JSONObject put(HttpServletRequest request, HttpServletResponse response, UserInfo user, boolean ifRemember) {
        long time;
        if (ifRemember) {
            time = 60 * 60 * 24 * 7L;
        } else {
            time = 30 * 60L;
        }
        //设置 JwtToken
        String userToken = JwtUtils.genJsonWebToken(user);
        if (userToken == null) throw new NullPointerException("--设置token失败");
        //转换dto层
        UserDto userDto = mapperFacade.map(user, UserDto.class);

        JSONObject uJson = new JSONObject();
        uJson.put("user", userDto);
        uJson.put("token", userToken);
        //设置token
        redisService.setString(Constants.TOKEN + ":" + userDto.getUid(), userToken, time);
        //设置Cookie
        CookieUtil.set(response, Constants.TOKEN, userToken, ifRemember);

        //设局 session
        request.getSession().setAttribute("token", userToken);
        request.getSession().setMaxInactiveInterval(120 * 60);
        String token = (String) request.getSession().getAttribute("token");
        System.out.println(token);
        return uJson;
    }

    private ResponseBase setLockingTime(UserInfo userInfo) {
        int errorNumber = 0;
        errorNumber++;
        int errorFre;
        long lockingTime;
        String errKey = Constants.ERROR_LOGIN + userInfo.getUserName();
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
            redisService.setString(Constants.TTL_DATE + userInfo.getUserName(), "error", lockingTime);
            return JsonData.setResultError("账号被锁定!" + lockingTime + "秒");
        }
        return JsonData.setResultError("账号或密码错误/你还有" + (4 - errorFre + "次机会"));
    }


//    @Override
//    public List<UserInfo> findByUsers(UserDto pageDto) {
//        return userMapper.findByUsers(pageDto);
//    }
//
//
//    @Override
//    public UserInfo getSingleUser(Long id) {
//        return userMapper.getSingleUser(id);
//    }
//
//
//    @Override
//    public int delUserInfo(String uidIds) {
//        return userMapper.delUserInfo(uidIds);
//    }
//
//    @Override
//    public int reUserInfo(String uidIds) {
//        return userMapper.reUserInfo(uidIds);
//    }
//
//    @Override
//    public List<UserInfo> findByDelUserInfo() {
//        return userMapper.findByDelUserInfo();
//    }
//
//    @Override
//    public UserInfo getUserName(String userName) {
//        return userMapper.getUserName(userName);
//    }
//
//
//    @Override
//    public List<UserInfo> getByUsers() {
//
//        return userMapper.getByUsers();
//    }
//
//    @Override
//    public int upUserPwd(Long uid, String pwd) {
//        return userMapper.upUserPwd(uid, pwd);
//    }


//    /**
//     * 创建新增用户
//     *
//     * @param userMap
//     * @return
//     */
//    @Override
//    @Transactional
//    @SuppressWarnings("unchecked")
//    public ResponseBase saveUserInfo(Map<String, Object> userMap) {
//        //获得登陆的时候 生成的token
//        String userName = (String) userMap.get("userName");
//        String pwd = (String) userMap.get("pwd");
//        Boolean checkedUpPwd = (Boolean) userMap.get("pwdAlways");
//        Boolean checkedUserAlways = (Boolean) userMap.get("uAlways");
//        Boolean checkedPwdAlways = (Boolean) userMap.get("pwdAlways");
//        Integer staffValue = (Integer) userMap.get("staffValue");
//        List<Integer> rolesId = (List<Integer>) userMap.get("rolesId");
//        //备注
//        String remark = (String) userMap.get("remark");
//        if (StringUtils.isBlank(userName) || StringUtils.isBlank(pwd) || checkedUpPwd == null
//                || checkedUserAlways == null || checkedPwdAlways == null || staffValue == null || rolesId == null) {
//            return JsonData.setResultError("新增失败");
//        }
//        //这里前端会传空字符串 或者 Long类型数据 要判断
//        UserInfo userInfo = new UserInfo();
//        userInfo.setRemark(remark);
//        userInfo.setCreateDate(new Date().getTime());
//        userInfo.setCreateUser(ReqUtils.getUserName());
//        //首次登陆是否修改密码
//        if (checkedUpPwd) {
//            userInfo.setFirstLogin(true);
//        } else {
//            userInfo.setFirstLogin(false);
//        }
//        userInfo.setUserName(userName);
//        String md5Pwd = MD5Util.saltMd5(userName, pwd);
//        userInfo.setPwd(md5Pwd);
//        //如果点击了   用户始终有效
//        if (checkedUserAlways) {
//            userInfo.setUserExpirationDate(0L);
//        } else {
//            Long userExpirationDate = (Long) userMap.get("userExpirationDate");
//            //设置 用户有效时间
//            userInfo.setUserExpirationDate(userExpirationDate);
//        }
//        //如果点击了   密码始终有效
//        if (checkedPwdAlways) {
//            userInfo.setPwdValidityPeriod(0L);
//        } else {
//            //前台会传2个类型参数 根据判断转换 来设计 用户 密码有效时间
//            Integer pwdValidityPeriod = (Integer) userMap.get("pwdValidityPeriod");
//            userInfo.setPwdValidityPeriod(DateUtils.getRearDate(pwdValidityPeriod));
//        }
//        //新增用户
//        userMapper.saveUserInfo(userInfo);
//        Long uid = userInfo.getUid();
//        Long sid = staffValue.longValue();
//        //关联员工信息 更新
//        hrService.bindHrInfo(uid, sid);
//        //新增角色信息
//        List<UserRole> urList = new ArrayList<>();
//        UserRole userRole = new UserRole();
//        userRole.setUid(uid);
//        userRole.setRoleIds(rolesId);
//        urList.add(userRole);
//        userRoleService.addUserRole(urList);
//        return JsonData.setResultSuccess("新增成功");
//    }

//    /**
//     * 处理更新用户修改信息
//     *
//     * @param userMap
//     * @return
//     */
//    @Override
//    @Transactional
//    public ResponseBase updateUserInfo(Map<String, Object> userMap) {
//        //更新用户信息
//        int updateResult = userMapper.upUser(userMap);
//        if (updateResult != 1) {
//            return JsonData.setResultError("更新失败,请重新操作");
//        }
//        String uMobilePhone = (String) userMap.get("mobilePhone");
//        if (StringUtils.isNotBlank(uMobilePhone)) {
//            //更新员工信息
//            hrService.upHrInfo(userMap);
//        }
//        //先判断是否为空
//        String pwd = (String) userMap.get("pwd");
//        //如果不是空 说明已经修改了密码
//        if (StringUtils.isNotBlank(pwd)) {
//            Integer uid = (Integer) userMap.get("uid");
//            String userName = (String) userMap.get("userName");
//            //踢出用户 如果是null  说明没有这个用户在线
//            chatStore.kickOut(userName + "token", uid.longValue(),
//                    JsonUtils.getJsonTypeError("管理员修改密码,你被踢出",
//                            ChatType.KICK_OUT));
//        }
//        return JsonData.setResultSuccess("更新成功");
//    }

}
