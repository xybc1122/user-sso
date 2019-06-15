package com.wh.utils;

import com.wh.toos.Constants;

public class RedisUtils {


    /**
     * 生成user key
     *
     * @param key
     * @return
     */
    public static String redisTokenKey(String key) {
        return Constants.SSO_TOKEN + ":" + key;
    }

    /**
     * 登陆成功后 删除Redis指定数据
     *
     * @param key
     * @return
     */
    public static String redisErrorKey(String key) {
        return Constants.ERROR_LOGIN + ":" + key;
    }
}

