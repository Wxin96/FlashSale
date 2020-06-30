package com.apollo.flashsale.result.key.impl;

public class FlashSaleUserKey extends BasePrefix {

    // 两天
    public static final int TOKEN_EXPIRE = 3600 * 24 * 2;

    public FlashSaleUserKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static FlashSaleUserKey token = new FlashSaleUserKey(TOKEN_EXPIRE, "tk");

}
