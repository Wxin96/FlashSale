package com.apollo.flashsale.result.key.impl;

import com.apollo.flashsale.result.key.KeyPrefix;

/**
 *  前缀抽象类
 */
public class BasePrefix implements KeyPrefix {
    private int expireSeconds;

    private String prefix;

    // 0代表永不过期
    public BasePrefix(String prefix) {
        this(0, prefix);
    }

    public BasePrefix(int expireSeconds, String prefix) {
        this.expireSeconds = expireSeconds;
        this.prefix = prefix;
    }

    @Override
    public int expireSeconds() {
        return expireSeconds;
    }

    /**
     * 根据类名, 组合前缀
     * @return
     */
    @Override
    public String getPrefix() {
        String className = this.getClass().getSimpleName();
        return className + ":" + prefix;
    }
}
