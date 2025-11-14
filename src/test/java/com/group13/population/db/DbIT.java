package com.group13.population.db;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration test for Db.connect.
 *
 * - On CI (CI=true) this test MUST run. If the DB is not reachable, the build fails.
 * - Locally, if the DB container is not running, the test is SKIPPED so
 *   `mvn verify` does not fail just because MySQL isn't up.
 */
class DbIT {

    private static String host() {
        // Prefer env (for CI), then system property, then localhost.
        String value = System.getenv("DB_HOST");
        if (value == null || value.isBlank()) {
            value = System.getProperty("DB_HOST");
        }
        if (value == null || value.isBlank()) {
            value = "localhost";
        }
        return value;
    }

    private static int port() {
        // Prefer env (for CI), then system property, then local default 43306.
        String raw = System.getenv("DB_PORT");
        if (raw == null || raw.isBlank()) {
            raw = System.getProperty("DB_PORT");
        }
        if (raw == null || raw.isBlank()) {
            raw = "43306"; // host port from docker-compose
        }
        return Integer.parseInt(raw.trim());
    }

    @Test
    void connectWithValidConfig() throws Exception {
        boolean isCi = "true".equalsIgnoreCase(System.getenv("CI"));
        String h = host();
        int p = port();

        try (Connection c = Db.connect(h, p, "world", "app", "app")) {
            assertNotNull(c, "Connection should not be null");
            assertFalse(c.isClosed(), "Connection should be open");
        } catch (Exception ex) {
            if (!isCi) {
                // Local: DB not running → skip, don’t fail verify.
                Assumptions.assumeTrue(false,
                    "Skipping DbIT – DB not reachable on "
                        + h + ":" + p + " (" + ex.getClass().getSimpleName() + ": " + ex.getMessage() + ")");
            } else {
                // CI: DB must be running. Rethrow so CI fails loudly.
                throw ex;
            }
        }
    }
}
