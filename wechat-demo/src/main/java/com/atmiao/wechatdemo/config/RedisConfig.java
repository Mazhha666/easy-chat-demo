package com.atmiao.wechatdemo.config;



import lombok.extern.slf4j.Slf4j;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author miao
 * @version 1.0
 */
@Configuration
@Slf4j
public class RedisConfig {
    @Value("${spring.data.redis.host:}")
    private String redisHost;
    @Value("${spring.data.redis.port:}")
    private Integer redisPort;
    @Value("${spring.data.redis.password}")
    private String redisPassword;

    @Bean(name= "redissonClient",destroyMethod = "shutdown")
    public RedissonClient redissonClient(){
            Config config = new Config();
            config.useSingleServer().setAddress("redis://" + redisHost + ":" + redisPort).setPassword(redisPassword);
            RedissonClient redissonClient = Redisson.create(config);
            return redissonClient;

    }

    @Bean("redisTemplate")
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory lettuceConnectionFactory)
    {
        RedisTemplate<String,Object> redisTemplate = new RedisTemplate<>();

        redisTemplate.setConnectionFactory(lettuceConnectionFactory);
        //设置key序列化方式string
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        //设置value的序列化方式json，使用GenericJackson2JsonRedisSerializer替换默认序列化
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }
}
