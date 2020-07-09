package com.apollo.flashsale.access;

import com.apollo.flashsale.domain.FlashSaleUser;

/**
 *  线程局部存储秒杀用户
 */
public class UserContext {

    private static ThreadLocal<FlashSaleUser> userHolder = new ThreadLocal<>();

    public static FlashSaleUser getUser() {
        return userHolder.get();
    }

    public static void setUser(FlashSaleUser user) {
        userHolder.set(user);
    }
}
