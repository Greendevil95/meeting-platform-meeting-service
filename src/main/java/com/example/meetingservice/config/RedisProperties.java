package com.example.meetingservice.config;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "meeting.redis")
@Getter
@Setter
public class RedisProperties {

    private Duration meetingTtl;
}
