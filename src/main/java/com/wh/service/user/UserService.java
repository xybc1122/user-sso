package com.wh.service.user;


import com.baomidou.mybatisplus.extension.service.IService;
import com.wh.base.ResponseBase;
import com.wh.entity.user.UserInfo;
import org.springframework.validation.BindingResult;


import javax.servlet.http.HttpServletRequest;

public interface UserService extends IService<UserInfo> {


    //app 用户认证
ResponseBase doGetAuthenticationInfo(HttpServletRequest request,UserInfo userInfo,BindingResult bindingResult);

}
