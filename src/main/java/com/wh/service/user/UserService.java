package com.wh.service.user;


import com.baomidou.mybatisplus.extension.service.IService;
import com.wh.base.ResponseBase;
import com.wh.entity.user.UserInfo;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface UserService extends IService<UserInfo> {

//    //web 用户认证
//    void doGetAuthenticationInfo(HttpServletRequest request, HttpServletResponse response, UserInfo userInfo);
    //app 用户认证
ResponseBase doGetAuthenticationInfo(HttpServletRequest request, HttpServletResponse response, UserInfo userInfo);

}
