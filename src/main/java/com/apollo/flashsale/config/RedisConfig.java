package com.apollo.flashsale.config;

import com.apollo.flashsale.redis.RedisProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
@EnableConfigurationProperties(RedisProperties.class)
public class RedisConfig {

    @Autowired
    RedisProperties redisProperties;

    @Bean
    public JedisPool JedisPoolFactory() {
        // 1.JedisPoolConfig JedisPool的配置类
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(redisProperties.getPoolMaxIdle());
        config.setMaxTotal(redisProperties.getPoolMaxTotal());
        config.setMaxWaitMillis(redisProperties.getPoolMaxWait());
        // 2.注册JedisPool
        // 参数中的timeout需要毫秒
        JedisPool jedisPool = new JedisPool(config, redisProperties.getHost(), redisProperties.getPort(),
                redisProperties.getTimeout()*1000, redisProperties.getPassword(), 0);
        return jedisPool;
    }


}
