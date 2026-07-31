package com.example.meetingservice.service;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OutboxTraceContextTest {

    private static final String TRACE_ID = "4bf92f3577b34da6a3ce929d0e0e4736";
    private static final String SPAN_ID = "00f067aa0ba902b7";
    private static final String TRACEPARENT = "00-" + TRACE_ID + "-" + SPAN_ID + "-01";

    @Test
    void currentTraceparentUsesCurrentSpanContext() {
        SpanContext spanContext = SpanContext.create(
                TRACE_ID,
                SPAN_ID,
                TraceFlags.getSampled(),
                TraceState.getDefault()
        );

        try (Scope ignored = Context.current().with(Span.wrap(spanContext)).makeCurrent()) {
            assertEquals(TRACEPARENT, OutboxTraceContext.currentTraceparent());
        }
    }

    @Test
    void makeCurrentRestoresTraceparentAsRemoteParent() {
        try (Scope ignored = OutboxTraceContext.makeCurrent(TRACEPARENT)) {
            SpanContext spanContext = Span.current().getSpanContext();

            assertEquals(TRACE_ID, spanContext.getTraceId());
            assertEquals(SPAN_ID, spanContext.getSpanId());
            assertTrue(spanContext.isRemote());
        }
    }
}
