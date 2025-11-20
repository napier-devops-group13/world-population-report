package com.group13.population.db;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link Db} against the real MySQL {@code world} database.
 *
 * These rely on docker-compose's `db` service exposing port 43306 on the host.
 */
public class DbIT {

    private static Db db;

    @BeforeAll
    @DisplayName("Connect to database before running Db integration tests")
    static void setUp() {
        db = new Db();

        String host = getenvOrDefault("DB_HOST", "localhost");
        String port = getenvOrDefault("DB_PORT", "43306");
        String location = host + ":" + port;

        boolean connected = db.connect(location, 30_000);
        assertTrue(connected, "Failed to connect to database at " + location);
    }

    @AfterAll
    static void tearDown() {
        if (db != null) {
            db.disconnect();
        }
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Db.getConnection returns a valid JDBC connection")
    void getConnectionReturnsValidConnection() throws Exception {
        Connection conn = db.getConnection();
        assertNotNull(conn, "Connection should not be null");
        assertFalse(conn.isClosed(), "Connection should be open");
    }

    @Test
    @DisplayName("Db.getConnection reuses the same connection instance while open")
    void getConnectionCachesConnection() throws Exception {
        Connection first = db.getConnection();
        Connection second = db.getConnection();

        assertSame(first, second,
            "Db.getConnection should return the same instance while it is open");
    }

    @Test
    @DisplayName("Db can execute a simple query against the world.country table")
    void canQueryCountryTable() throws Exception {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM country");
             ResultSet rs = ps.executeQuery()) {

            assertTrue(rs.next(), "Expected one row from COUNT(*)");
            long count = rs.getLong(1);
            assertTrue(count > 0, "Expected at least one country row, got " + count);
        }
    }

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
