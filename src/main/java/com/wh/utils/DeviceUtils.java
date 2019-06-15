package com.wh.utils;

import javax.servlet.http.HttpServletRequest;

public class DeviceUtils {


    /**
     *      * 判断请求是PC 还是手机端
     *      * @param requestHeader
     *      * @return
     *     
     */
    public static boolean isMobileDevice(HttpServletRequest request) {
        String requestHeader = request.getHeader("user-agent");
        String[] deviceArray = new String[]{"android", "mac os", "windows phone"};
        if (requestHeader == null) {
            return false;
        }
        requestHeader = requestHeader.toLowerCase();
        for (String device : deviceArray) {
            if (requestHeader.contains(device)) {
                return true;
            }
        }
        return false;
    }


}
