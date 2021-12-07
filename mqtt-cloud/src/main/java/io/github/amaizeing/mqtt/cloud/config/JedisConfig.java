package io.github.amaizeing.mqtt.cloud.config;

import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
class JedisConfig {

  JedisConnectionFactory jedisConnectionFactory() {
    val config = new RedisStandaloneConfiguration();
    config.setHostName("redis-server");
    config.setPort(6379);
    val factory = new JedisConnectionFactory(config);
    factory.afterPropertiesSet();
    return factory;
  }

  @Bean
  StringRedisTemplate redisTemplate() {
    return new StringRedisTemplate(jedisConnectionFactory());
  }

}
