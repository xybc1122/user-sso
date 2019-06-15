package com.wh.store;

import java.util.UUID;

public class SsoSessionIdHelper {


    public static String parseStoreKey(String sessionId) {
        if (sessionId != null && sessionId.contains("_")) {
            String[] sessionIdArr = sessionId.split("_");
            if (sessionIdArr.length == 2
                    && sessionIdArr[0] != null
                    && sessionIdArr[0].trim().length() > 0) {
                return sessionIdArr[0].trim();
            }
        }
        return null;
    }
}
