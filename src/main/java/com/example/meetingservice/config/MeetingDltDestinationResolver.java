package com.example.meetingservice.config;

import com.example.meetingservice.metrics.MeetingMetrics;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;

import java.util.function.BiFunction;

public class MeetingDltDestinationResolver implements BiFunction<ConsumerRecord<?, ?>, Exception, TopicPartition> {

    private final String consumerGroupId;
    private final MeetingMetrics meetingMetrics;

    public MeetingDltDestinationResolver(String consumerGroupId, MeetingMetrics meetingMetrics) {
        this.consumerGroupId = consumerGroupId;
        this.meetingMetrics = meetingMetrics;
    }

    @Override
    public TopicPartition apply(ConsumerRecord<?, ?> record, Exception exception) {
        TopicPartition destination = new TopicPartition(
                consumerGroupId + ".dlt." + eventName(record.topic()),
                record.partition()
        );
        meetingMetrics.recordDltRouted(record.topic(), destination.topic(), exception);
        return destination;
    }

    private String eventName(String topic) {
        int delimiter = topic.indexOf('.');
        if (delimiter < 0 || delimiter == topic.length() - 1) {
            return topic;
        }
        return topic.substring(delimiter + 1);
    }
}
