package com.wh.toos;


public interface Constants {
    /**
     * token
     */
    String SSO_TOKEN = "sso-token";

    /**
     * 设置限制登陆时间key
     */
    String TTL_DATE = "ttlDate:";

    /**
     * 登陆次数超时redis key
     */
    String ERROR_LOGIN = "error_login:";
    /**
     * 响应请求成功
     */
    String HTTP_RES_CODE_200_VALUE = "success";
    /**
     * 响应请求成功code
     */
    Integer HTTP_RES_CODE_200 = 200;
    /**
     * 系统错误
     */
    Integer HTTP_RES_CODE = -1;

    String REDIRECT_URL = "redirect_url";


    /**
     * 租户key
     */
    String TENANT_KEY = "tenant_key";

}
