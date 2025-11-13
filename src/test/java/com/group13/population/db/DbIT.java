package com.group13.population.db;

import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for Db.connect using the real MySQL "world" database.
 * Requires the docker `db` service to be running.
 */
class DbIT {

    /** Read a system property, but fall back if it is missing or blank. */
    private static String prop(String key, String def) {
        String v = System.getProperty(key);
        return (v == null || v.isBlank()) ? def : v.trim();
    }

    @Test
    void connectWithValidConfig() throws Exception {
        // Same defaults as WorldRepoIT so it works locally and in CI.
        String host   = prop("DB_HOST", "localhost");
        String portS  = prop("DB_PORT", "43306");
        int    port   = Integer.parseInt(portS);
        String dbName = prop("DB_NAME", "world");
        String user   = prop("DB_USER", "app");
        String pass   = prop("DB_PASS", "app");

        try (Connection c = Db.connect(host, port, dbName, user, pass)) {
            assertFalse(c.isClosed(), "Connection should be open");
        }
    }
}
