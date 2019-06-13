package com.wh.utils;

import com.wh.exception.LsException;

/**
 * @ClassName JsonUtils
 * Description TODO
 * @Author 陈恩惠
 * @Date 2019/3/28 13:40
 **/
public class JsonUtils {

    /**
     * 如果新增失败直接报错
     *
     * @param result
     * @return
     */
    public static void saveResult(int result) {
        if (result == 0) {
            throw new LsException("error");
        }
    }
}
