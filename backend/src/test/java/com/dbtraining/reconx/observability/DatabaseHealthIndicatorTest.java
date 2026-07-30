package com.dbtraining.reconx.observability;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ============================================================================
 * TICKET-ADV059 — DatabaseHealthIndicator smoke test
 * ============================================================================
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.liquibase.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class DatabaseHealthIndicatorTest {

    @Autowired
    private DatabaseHealthIndicator databaseHealthIndicator;

    @Test
    void health_reportsUpWithQueryAndElapsedMs() {
        var health = databaseHealthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("query", "SELECT 1");
        assertThat(health.getDetails()).containsKey("elapsedMs");
    }
}