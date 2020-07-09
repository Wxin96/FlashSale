package com.apollo.flashsale.redis.key.impl;

public class FlashSaleKey extends BasePrefix {

    /**
     * 默认永久有效
     * @param prefix 前缀
     */
    public FlashSaleKey(String prefix) {
        super(prefix);
    }

    /**
     *  设置过期时间
     * @param expireSeconds 有效时长, 单位: s
     * @param prefix 前缀
     */
    public FlashSaleKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    // 判断当前商品是否已经秒杀完毕, 键的对应值是 boolean类型
    public static FlashSaleKey isGoodsOver = new FlashSaleKey("go");

    // 临时保存秒杀的请求路径, 设置有效时间60秒; 键的对应值为 字符串路径(uuid、mdk和salt配合生成)
    public static FlashSaleKey getFlashSalePath = new FlashSaleKey(60, "fsPath");

    // 临时保存验证码的键值, 设置有效时间为 60秒; 键的对应值是 int 类型
    public static FlashSaleKey getFlashSaleVerifyCode = new FlashSaleKey(60, "imgVc");

}
