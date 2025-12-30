package com.se114p12.backend.configs;

import com.se114p12.backend.services.authentication.InMemoryLoginAttemptService;
import com.se114p12.backend.services.authentication.LoginAttemptService;
import com.se114p12.backend.services.authentication.RedisLoginAttemptService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

  @Value("${redis.enabled}")
  private boolean redisEnabled;

  @Value("${spring.data.redis.host}")
  private String host;

  @Value("${spring.data.redis.port}")
  private int port;

  @Bean
  LettuceConnectionFactory redisConnectionFactory() {
    return new LettuceConnectionFactory(new RedisStandaloneConfiguration(host, port));
  }

  @Bean
  RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
    RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(redisConnectionFactory);
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
    return redisTemplate;
  }

  @Bean
  @ConditionalOnProperty(name = "redis.enabled", havingValue = "true", matchIfMissing = true)
  public LoginAttemptService redisLoginAttemptService(RedisTemplate<String, Object> template) {
    return new RedisLoginAttemptService(template);
  }

  @Bean
  @ConditionalOnProperty(name = "redis.enabled", havingValue = "false")
  public LoginAttemptService inMemoryLoginAttemptService() {
    System.out.println("Using in-memory login attempt service as Redis is not configured.");
    System.out.println("Replace redis with ben-manes/caffeine-cache for better performance.");
    return new InMemoryLoginAttemptService();
  }
}
