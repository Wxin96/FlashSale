package com.apollo.flashsale.redis;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "redis")
public class RedisProperties {
    // Redis主机
    private String host;
    // Redis端口号
    private int port;

    private int timeout;//秒
    // 密码
    private String password;
    // 最大连接数量
    private int poolMaxTotal;
    // 连接池最多空闲数目
    private int poolMaxIdle;
    // 连接池最大等待时间
    private int poolMaxWait;//秒
}
