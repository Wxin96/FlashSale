package com.apollo.flashsale.redis.key;

/**
 *  前缀接口
 */
public interface KeyPrefix {
    // 过期时间
    int expireSeconds();
    // 前缀
    String getPrefix();

}
