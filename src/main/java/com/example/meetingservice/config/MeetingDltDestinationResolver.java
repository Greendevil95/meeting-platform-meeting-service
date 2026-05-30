package com.example.meetingservice.config;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;

import java.util.function.BiFunction;

public class MeetingDltDestinationResolver implements BiFunction<ConsumerRecord<?, ?>, Exception, TopicPartition> {

    private final String consumerGroupId;

    public MeetingDltDestinationResolver(String consumerGroupId) {
        this.consumerGroupId = consumerGroupId;
    }

    @Override
    public TopicPartition apply(ConsumerRecord<?, ?> record, Exception exception) {
        return new TopicPartition(consumerGroupId + ".dlt." + eventName(record.topic()), record.partition());
    }

    private String eventName(String topic) {
        int delimiter = topic.indexOf('.');
        if (delimiter < 0 || delimiter == topic.length() - 1) {
            return topic;
        }
        return topic.substring(delimiter + 1);
    }
}
