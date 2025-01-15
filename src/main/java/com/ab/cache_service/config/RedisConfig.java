package com.ab.cache_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * Redis config
 */
@Configuration
public class RedisConfig {

    @Value("${spring.cache.host}")
    private String redisHostName;

    @Value("${spring.cache.port}")
    private int redisPort;

    @Value("${redis.nodes}")
    private String redisNodes;

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration();
        return configureJedisConnectionFactory(redisClusterConfiguration);
    }

    private JedisConnectionFactory configureJedisConnectionFactory(RedisClusterConfiguration redisClusterConfiguration) {
        String[] hostAndPorts = StringUtils.commaDelimitedListToStringArray(redisNodes);
        Set<RedisNode> clusterNodes = new HashSet<>();
        for (String hostAndPort : hostAndPorts) {
            int lastScIndex = hostAndPort.lastIndexOf(":");
            if (lastScIndex == -1) continue;

            try {
                String host = hostAndPort.substring(0, lastScIndex);
                Integer port = Integer.parseInt(hostAndPort.substring(lastScIndex + 1));
                clusterNodes.add(new RedisNode(host, port));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (hostAndPorts.length > 0) {
            redisClusterConfiguration.setClusterNodes(clusterNodes);
            redisClusterConfiguration.setMaxRedirects(3);
        }else {
            redisClusterConfiguration.setClusterNodes(new HashSet<>());
            redisClusterConfiguration.setMaxRedirects(3);
        }
        return new JedisConnectionFactory(redisClusterConfiguration);
    }

    @Bean
    public RedisTemplate<Object, Object> redisTemplate() {
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(jedisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new JdkSerializationRedisSerializer());
        redisTemplate.setHashValueSerializer(new JdkSerializationRedisSerializer());
        redisTemplate.setEnableTransactionSupport(false);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
