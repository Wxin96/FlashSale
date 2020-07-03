package com.apollo.flashsale.redis.key.impl;

/**
 *  作用: 存储页面缓存 URL缓存]
 *  设置:
 *      有效时间 60 秒
 */
public class GoodsKey extends BasePrefix {

    private static final int EXPIRED_SECONDS = 60;

    public GoodsKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    // 存储物品列表的Redis键值前缀
    public static GoodsKey getGoodsList = new GoodsKey(EXPIRED_SECONDS, "gl");

    // 物品细节页面的Redis键值前缀
    public static GoodsKey getGoodsDetail = new GoodsKey(EXPIRED_SECONDS, "gd");

}
