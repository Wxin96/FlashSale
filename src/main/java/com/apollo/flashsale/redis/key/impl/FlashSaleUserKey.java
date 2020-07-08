package com.apollo.flashsale.redis.key.impl;

/**
 *  作用: 存储FlashSale用户信息
 *  细节:
 *      有效期 两天
 */
public class FlashSaleUserKey extends BasePrefix {

    // 两天
    // public static final int TOKEN_EXPIRE = 3600 * 24 * 2;
    public static final int TOKEN_EXPIRE = 0;

    public FlashSaleUserKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    // 分布式Session, 利用Cookie查询用户登录
    public static FlashSaleUserKey token = new FlashSaleUserKey(TOKEN_EXPIRE, "tk");

    // 用户信息缓存, 永久有效
    public static FlashSaleUserKey getById = new FlashSaleUserKey(0, "id");


}
