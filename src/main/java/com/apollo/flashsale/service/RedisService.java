package com.apollo.flashsale.service;

import com.alibaba.fastjson.JSON;
import com.apollo.flashsale.result.key.KeyPrefix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
public class RedisService {

    @Autowired
    JedisPool jedisPool;

    /**
     * 获取单个对象
     * @param prefix 前缀
     * @param key id
     * @param clazz 实体类型字节码
     * @param <T> 实体类型泛型
     * @return
     */
    public <T> T get(KeyPrefix prefix, String key, Class<T> clazz) {
        Jedis jedis = null;
        try {
            // 1.获取链接
            jedis = jedisPool.getResource();
            // 2.生成真正的key
            String realKey = prefix.getPrefix() + key;
            String str = jedis.get(realKey);
            T t = stringToBean(str, clazz);
            return t;
        } finally {
            returnToPool(jedis);
        }
    }

    /**
     *  设置对象
     * @param prefix 前缀
     * @param key id
     * @param value 封装对象
     * @param <T> 封装对象泛型
     * @return 成功返回true
     */
    public <T> boolean set(KeyPrefix prefix, String key, T value) {
        Jedis jedis = null;
        try {
            // 1.获取链接
            jedis = jedisPool.getResource();
            // 2.生成真正的key
            String realKey = prefix.getPrefix() + key;
            // 3.POJO转JSON字符串
            String strValue = beanToString(value);
            // 4.获取保存时间
            int expireSeconds = prefix.expireSeconds();
            if (expireSeconds <= 0) {
                jedis.set(realKey, strValue);
            } else {
                jedis.setex(realKey, expireSeconds, strValue);
            }
            return true;
        } finally {
            returnToPool(jedis);
        }
    }

    /**
     *  判断key是否存在
     * @param prefix 前缀
     * @param key id
     * @return 存在返回true, 不存在返回false
     */
    public boolean exists(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            // 1.获取链接
            jedis = jedisPool.getResource();
            // 2.生成真正的key
            String realKey = prefix.getPrefix() + key;
            // 3.判断是否村子啊
            return jedis.exists(realKey);
        } finally {
            returnToPool(jedis);
        }
    }

    /**
     * 增加值
     * @param prefix 前缀
     * @param key id
     * @return 增加后的数值
     */
    public <T> Long incr(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            // 1.获取链接
            jedis = jedisPool.getResource();
            // 2.生成真正的key
            String realKey = prefix.getPrefix() + key;
            // 3.判断是否村子啊
            return jedis.incr(realKey);
        } finally {
            returnToPool(jedis);
        }
    }

    /**
     * 减小值
     * @param prefix 前缀
     * @param key id
     * @return 减小后的数值
     */
    public <T> Long decr(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            // 1.获取链接
            jedis = jedisPool.getResource();
            // 2.生成真正的key
            String realKey = prefix.getPrefix() + key;
            // 3.判断是否村子啊
            return jedis.decr(realKey);
        } finally {
            returnToPool(jedis);
        }
    }


    /**
     * JSON字符串转换为实体类对象
     * @param str JSON字符串
     * @param clazz 要转化类的字节码
     * @param <T> 要转化类的泛型
     * @return 返回实体类对象
     */
    @SuppressWarnings("unchecked")
    private <T> T stringToBean(String str, Class<T> clazz) {
        // 0.预处理
        if (str == null || str.length() <= 0 || clazz == null) {
            return null;
        }
        // 1.分类处理
        if (clazz == int.class || clazz == Integer.class) {
            return (T) Integer.valueOf(str);
        } else if (clazz == String.class) {
            return (T) str;
        } else if (clazz == long.class || clazz == Long.class) {
            return (T) Long.valueOf(str);
        } else {
            // POJO类型
            return JSON.toJavaObject(JSON.parseObject(str), clazz);
        }
    }

    /**
     *  实体类对象转化为字符串
     * @param value 实体类对象
     * @param <T> 实体类对象泛型
     * @return JSON字符串
     */
    private <T> String beanToString(T value) {
        // 0.预处理
        if (value == null) {
            return null;
        }
        // 1.分类处理
        Class<?> clazz = value.getClass();
        if (clazz == Integer.class) {
            return "" + value;
        } else if (clazz == String.class) {
            return (String) value;
        } else if (clazz == Long.class) {
            return "" + value;
        } else {
            // POJO类型
            return JSON.toJSONString(value);
        }
    }

    /**
     *  将Jedis连接返回JedisPool中
     * @param jedis 单个jedis连接
     */
    private void returnToPool(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }


}