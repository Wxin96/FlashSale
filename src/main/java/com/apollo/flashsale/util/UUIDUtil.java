package com.apollo.flashsale.util;

import java.util.UUID;

/**
 *  生成UUID
 */
public class UUIDUtil {
    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
