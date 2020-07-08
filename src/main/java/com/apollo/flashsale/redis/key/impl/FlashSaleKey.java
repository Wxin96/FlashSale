package com.apollo.flashsale.redis.key.impl;

public class FlashSaleKey extends BasePrefix {

    public FlashSaleKey(String prefix) {
        super(prefix);
    }

    // 判断当前商品是否已经秒杀完毕
    public static FlashSaleKey isGoodsOver = new FlashSaleKey("go");

}
