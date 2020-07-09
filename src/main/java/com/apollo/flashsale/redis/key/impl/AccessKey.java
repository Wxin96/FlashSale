package com.apollo.flashsale.redis.key.impl;

/**
 * 注解@AccessLimit标定路径的请求次数限定key
 */
public class AccessKey extends BasePrefix{

    public AccessKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    /**
     *  根据不同的expireSeconds时间, 返回AccessKey前缀类
     * @param expireSeconds 键值的保存时间
     * @return AccessKey前缀类
     */
    public static AccessKey withExpire(int expireSeconds) {
        return new AccessKey(expireSeconds, "access");
    }
}
