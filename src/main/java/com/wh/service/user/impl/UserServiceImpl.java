package com.wh.service.user.impl;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wh.base.JsonData;
import com.wh.base.ResponseBase;
import com.wh.dds.DynamicDataSourceContextHolder;
import com.wh.dto.TenantStateDto;
import com.wh.dto.UserDto;
import com.wh.entity.role.WhUserRole;
import com.wh.entity.user.UserInfo;
import com.wh.exception.LsException;
import com.wh.feign.TenantFeignClient;
import com.wh.mapper.UserMapper;
import com.wh.service.tenant.TenantService;
import com.wh.store.BindingResultStore;
import com.wh.service.redis.RedisService;
import com.wh.service.role.IWhUserRoleService;
import com.wh.service.user.UserService;
import com.wh.utils.*;
import ma.glasnost.orika.MapperFacade;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;


@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserInfo> implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private IWhUserRoleService roleService;

    @Autowired
    private TenantFeignClient feignClient;

    @Autowired
    private TenantService tenantService;

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
     * @return
     */
    @Override
    public ResponseBase doGetAuthenticationInfo(HttpServletRequest request, UserInfo userInfo, BindingResult bindingResult) {
        //1校验参数
        String strBinding = BindingResultStore.bindingResult(bindingResult);
        if (strBinding != null) return JsonData.setResultError(strBinding);

        //重置数据源  不重置会出问题
        DynamicDataSourceContextHolder.clearDataSourceKey();

        Long ttlDate = redisService.getTtl(RedisService.redisTTLKey(userInfo.getUserName(), userInfo.getTenant()));
        //如果不等于null
        if (ttlDate != -2) {
            return JsonData.setResultError("账号/或密码错误被锁定/" + ttlDate + "秒后到期!");
        }
        //这里去通过tenant 主表是查询标识是否这个租户还有效
        TenantStateDto tenantDto = feignClient.selTenantStatus(userInfo.getTenant());

        if (tenantDto == null || tenantDto.gettStatus() != 0) {
            return JsonData.setResultError("没有此租户/租户已经冻结");
        } else if (tenantDto.getEffectiveTime() != 0 && tenantDto.getEffectiveTime() < new Date().getTime()) {
            return JsonData.setResultError("租户已过期");
        }
        //切换租户
        tenantService.switchTenant(userInfo.getTenant());

        String md5Pwd = MD5Util.saltMd5(userInfo.getUserName(), userInfo.getPwd());
        //查询用户信息 更新更新登陆时间
        LambdaQueryWrapper<UserInfo> lambdaQuery = WrapperUtils.getLambdaQuery();
        lambdaQuery.eq(UserInfo::getUserName, userInfo.getUserName()).eq(UserInfo::getPwd, md5Pwd).eq(UserInfo::getTenant, userInfo.getTenant());
        UserInfo user = userMapper.selectOne(lambdaQuery);
        try {
            // 账号不存在 异常
            if (user == null) {
                throw new LsException("账号/密码/租户标识错误/没找到帐号,登录失败");
            }
            if (StringUtils.isBlank(user.getTenant()) || user.gettId() == null) {
                return JsonData.setResultError("此账号没有配置租户");
            }
            if (user.getAccountStatus() == 1) {
                return JsonData.setResultError("账号已停用,请联系管理员");
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
            boolean update = this.lambdaUpdate().
                    set(UserInfo::getLandingTime, new Date().getTime()).
                    eq(UserInfo::getUid, user.getUid()).update();
            JsonUtils.saveResult(update);
            //设置token
            JSONObject uJson = put(user, userInfo.isRememberMe());
            //登陆成功后 删除Redis指定数据
            redisService.delKey(RedisService.redisErrorKey(user.getUserName()));
            return JsonData.setResultSuccess(uJson);
        } catch (LsException ls) {
            return setLockingTime(userInfo.getUserName(), userInfo.getTenant());
        }
    }

    private JSONObject put(UserInfo user, boolean ifRemember) {
        long time;
        if (ifRemember) {
            time = 60 * 60 * 24 * 7L;
        } else {
            time = 60 * 60 * 24L;
        }
        //设置 JwtToken
        String token = JwtUtils.genJsonWebToken(user);
        //转换dto层
        UserDto userDto = mapperFacade.map(user, UserDto.class);

        JSONObject uJson = new JSONObject();
        uJson.put("user", userDto);
        uJson.put("token", token);

        //设置token
        redisService.setString(RedisService.redisTokenKey(user.getUid().toString(), user.getTenant()), token, time);

        //设置Cookie
        // CookieUtil.set(response, Constants.SSO_TOKEN, token, ifRemember);

        return uJson;
    }

    private ResponseBase setLockingTime(String userName, String tenant) {
        int errorNumber = 0;
        errorNumber++;
        int errorFre;
        long lockingTime;
        String errKey = RedisService.redisErrorKey(userName);
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
            redisService.setString(RedisService.redisTTLKey(userName, tenant), "error", lockingTime);
            return JsonData.setResultError("账号被锁定!" + lockingTime + "秒");
        }
        return JsonData.setResultError("账号或密码错误/你还有" + (4 - errorFre + "次机会"));
    }

}
