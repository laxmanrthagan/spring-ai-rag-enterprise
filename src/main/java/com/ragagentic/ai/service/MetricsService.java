package com.ragagentic.ai.service;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MetricsService {

    private final MeterRegistry meterRegistry;

    public void recordLatency(String trackingMetricName, long executionDurationMs) {
        meterRegistry.timer(trackingMetricName).record(executionDurationMs, TimeUnit.MILLISECONDS);
    }

    public void incrementCounter(String tracingCounterMetricName) {
        meterRegistry.counter(tracingCounterMetricName).increment();
    }
}