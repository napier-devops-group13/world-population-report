package com.group13.population.repo;

import com.group13.population.db.Db;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link PopulationRepo} against the real MySQL {@code world} database.
 *
 * These rely on docker-compose's `db` service exposing a port on the host
 * (default 43306, but can be overridden via DB_HOST / DB_PORT env vars).
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PopulationRepoIT {

    private Db db;
    private PopulationRepo repo;

    @BeforeAll
    @DisplayName("Connect to database before running PopulationRepo integration tests")
    void setUp() {
        db = new Db();

        String host = getenvOrDefault("DB_HOST", "localhost");
        String port = getenvOrDefault("DB_PORT", "43306");
        String location = host + ":" + port;

        boolean connected = db.connect(location, 30_000);
        assertTrue(connected, "Failed to connect to database at " + location);

        repo = new PopulationRepo(db);
    }

    @AfterAll
    void tearDown() {
        if (db != null) {
            db.disconnect();
        }
    }

    // -------------------------------------------------------------------------
    // Your existing @Test methods stay here unchanged
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static String getenvOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }
}
