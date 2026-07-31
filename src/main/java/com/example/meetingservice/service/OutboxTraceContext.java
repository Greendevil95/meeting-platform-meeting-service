package com.example.meetingservice.service;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;

final class OutboxTraceContext {

    private static final int TRACEPARENT_PARTS = 4;

    private OutboxTraceContext() {
    }

    static String currentTraceparent() {
        SpanContext spanContext = Span.current().getSpanContext();
        if (!spanContext.isValid()) {
            return null;
        }
        return "00-%s-%s-%s".formatted(
                spanContext.getTraceId(),
                spanContext.getSpanId(),
                spanContext.getTraceFlags().asHex()
        );
    }

    static Scope makeCurrent(String traceparent) {
        SpanContext spanContext = parseTraceparent(traceparent);
        if (!spanContext.isValid()) {
            return () -> {
            };
        }
        return Context.current().with(Span.wrap(spanContext)).makeCurrent();
    }

    private static SpanContext parseTraceparent(String traceparent) {
        if (traceparent == null || traceparent.isBlank()) {
            return SpanContext.getInvalid();
        }

        String[] parts = traceparent.split("-");
        if (parts.length != TRACEPARENT_PARTS
                || parts[0].length() != 2
                || parts[1].length() != 32
                || parts[2].length() != 16
                || parts[3].length() != 2) {
            return SpanContext.getInvalid();
        }

        try {
            return SpanContext.createFromRemoteParent(
                    parts[1],
                    parts[2],
                    TraceFlags.fromHex(parts[3], 0),
                    TraceState.getDefault()
            );
        } catch (IllegalArgumentException ex) {
            return SpanContext.getInvalid();
        }
    }
}
