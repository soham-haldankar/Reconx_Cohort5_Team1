package com.dbtraining.reconx.observability;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * TICKET-ADV084 — Timer: reconciliation_duration_seconds
 * ============================================================================
 */
@Component
public class ReconMetrics {

    private final Timer reconciliationTimer;

    public ReconMetrics(MeterRegistry registry) {
        // Build a Timer that publishes percentile histograms for server-side PromQL queries.
        // histogram=true publishes _bucket series with le (less-than-or-equal) boundaries.
        // publishPercentiles publishes client-side percentile estimates.
        this.reconciliationTimer = Timer.builder("reconciliation.duration")
                .description("Wall time of reconciliation batch processing")
                .publishPercentileHistogram()
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
    }

    /**
     * Return the timer so callers can wrap engine calls: 
     * reconMetrics.reconciliationTimer().record(() -> engine.reconcile(...))
     */
    public Timer reconciliationTimer() {
        return reconciliationTimer;
    }
}