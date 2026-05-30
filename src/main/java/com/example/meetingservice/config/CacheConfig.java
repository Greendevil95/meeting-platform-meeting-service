package com.example.meetingservice.config;


import com.example.meetingservice.api.dto.MeetingResponse;
import java.time.Duration;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.json.JsonMapper;


@Configuration
public class CacheConfig {

    public static final String MEETING_CACHE_NAME = "meeting";

    @Bean
    public CacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            JsonMapper jsonMapper,
            RedisProperties redisProperties
    ) {
        RedisCacheConfiguration config = meetingCacheConfiguration(jsonMapper, redisProperties.getMeetingTtl());

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }

    static RedisCacheConfiguration meetingCacheConfiguration(JsonMapper jsonMapper, Duration ttl) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new JacksonJsonRedisSerializer<>(jsonMapper, MeetingResponse.class)
                ));
    }
}
