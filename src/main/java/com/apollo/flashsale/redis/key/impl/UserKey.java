package com.apollo.flashsale.redis.key.impl;

public class UserKey extends BasePrefix {

    public UserKey(String prefix) {
        super(prefix);
    }
    // 静态方法效果: "UserKey" + ":" + "id"
    public static UserKey getById = new UserKey("id");
    // 静态方法效果: "UserKey" + ":" + "name"
    public static UserKey getByName = new UserKey("name");

}
