package com.example.meetingservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "meeting.kafka.topics")
@Getter
@Setter
public class KafkaTopicsProperties {

    private String userCreated;
    private String userUpdated;
    private String userDeleted;
    private String userStatusChanged;

    private String meetingCreated;
    private String meetingUpdated;
    private String meetingCancelled;
    private String meetingParticipantAdded;
    private String meetingParticipantRemoved;
}
