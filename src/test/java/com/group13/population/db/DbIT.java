package com.group13.population.db;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Simple integration test for Db.connect.
 * Used mainly for local coverage; on CI it is skipped because
 * WorldRepoIT already exercises Db.connect against MySQL.
 */
class DbIT {

    private static String host() {
        String value = System.getProperty("DB_HOST");
        if (value == null || value.isBlank()) {
            // Local default (docker-compose exposes world-db on localhost)
            value = "localhost";
        }
        return value;
    }

    private static int port() {
        String raw = System.getProperty("DB_PORT");
        if (raw == null || raw.isBlank()) {
            // Local default host-port from docker-compose: 43306 -> container 3306
            raw = "43306";
        }
        return Integer.parseInt(raw.trim());
    }

    @Test
    void connectWithValidConfig() throws Exception {
        // On GitHub Actions (CI=true) we skip this test – timing of the MySQL
        // service can be flaky, and Db.connect is already covered by WorldRepoIT.
        Assumptions.assumeFalse(
            "true".equalsIgnoreCase(System.getenv("CI")),
            "Skip DbIT on CI – Db.connect is covered via WorldRepoIT"
        );

        try (Connection c = Db.connect(host(), port(), "world", "app", "app")) {
            assertNotNull(c, "Connection should not be null");
            assertFalse(c.isClosed(), "Connection should be open");
        }
    }
}
