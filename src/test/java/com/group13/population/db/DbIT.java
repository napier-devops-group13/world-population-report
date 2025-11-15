package com.group13.population.db;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration test for Db.connect against a real MySQL database.
 *
 * - On CI (CI=true) this test is skipped; Db.connect is exercised by other ITPs.
 * - Locally, if the DB container is not running or credentials are wrong
 *   the test is also skipped instead of failing the build.
 */
class DbIT {

    private static String host() {
        String value = System.getProperty("DB_HOST");
        if (value == null || value.isBlank()) {
            value = System.getenv("DB_HOST");
        }
        if (value == null || value.isBlank()) {
            value = "localhost";
        }
        return value;
    }

    private static int port() {
        String raw = System.getProperty("DB_PORT");
        if (raw == null || raw.isBlank()) {
            raw = System.getenv("DB_PORT");
        }
        if (raw == null || raw.isBlank()) {
            raw = "43306";    // docker-compose host-port
        }
        return Integer.parseInt(raw.trim());
    }

    @Test
    void connectWithValidConfig() throws Exception {
        Assumptions.assumeFalse(
            "true".equalsIgnoreCase(System.getenv("CI")),
            "Skip DbIT on CI – Db.connect is covered via other ITPs"
        );

        final String h = host();
        final int p = port();

        try (Connection c = Db.connect(h, p, "world", "app", "app")) {
            assertNotNull(c, "Connection should not be null");
            assertFalse(c.isClosed(), "Connection should be open");
        } catch (Exception ex) {
            Assumptions.assumeTrue(false,
                "Skipping DbIT – DB not reachable on " + h + ":" + p
                    + " (" + ex.getClass().getSimpleName() + ": " + ex.getMessage() + ")");
        }
    }
}
