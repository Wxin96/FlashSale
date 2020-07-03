package com.apollo.flashsale.redis.key.impl;

public class OrderKey extends BasePrefix {

    public OrderKey(String prefix) {
        super(prefix);
    }

    public OrderKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static OrderKey getFlashSaleOrderKey = new OrderKey("fsOrder");
}
