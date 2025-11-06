package com.group13.population.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Small helper for creating JDBC connections to the seeded "world" database.
 * <p>
 * Reads configuration from environment variables:
 * DB_HOST (default 127.0.0.1)
 * DB_PORT (default 43306 to match docker-compose published port)
 * DB_NAME (default world)
 * DB_USER (default app)
 * DB_PASS (default app)
 */
public final class Db {

    private final String host;
    private final int port;
    private final String name;
    private final String user;
    private final String pass;
    private final String jdbcUrl;

    public Db() {
        this.host = getenvOrDefault("DB_HOST", "127.0.0.1");
        this.port = parseIntOrDefault("DB_PORT", 43306); // matches compose mapping
        this.name = getenvOrDefault("DB_NAME", "world");
        this.user = getenvOrDefault("DB_USER", "app");
        this.pass = getenvOrDefault("DB_PASS", "app");

        // keep SSL off for local dev, allow public key retrieval, set UTC tz
        this.jdbcUrl = String.format(
            "jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
            host, port, name);
    }

    /** Returns a live JDBC connection. Blocks briefly until DB is ready. */
    public Connection connect() throws SQLException {
        // Wait up to ~30 seconds for the database to become reachable
        awaitReady(30_000L, 300L);
        return DriverManager.getConnection(jdbcUrl, user, pass);
    }

    /**
     * Polls the DB until it responds to a simple "SELECT 1" or the timeout elapses.
     *
     * @param timeoutMillis total time to wait
     * @param sleepMillis   delay between attempts
     */
    public void awaitReady(final long timeoutMillis, final long sleepMillis) {
        final long deadline = System.currentTimeMillis() + timeoutMillis;
        SQLException last = null;

        while (System.currentTimeMillis() < deadline) {
            try (Connection c = DriverManager.getConnection(jdbcUrl, user, pass);
                 PreparedStatement ps = c.prepareStatement("SELECT 1");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return; // Ready
                }
            } catch (SQLException e) {
                last = e;
                // swallow and retry after a short sleep
            }
            try {
                Thread.sleep(sleepMillis);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Waiting for DB was interrupted.", ie);
            }
        }

        throw new IllegalStateException("Database not ready after wait.", last);
    }

    // ---------- helpers ----------

    private static String getenvOrDefault(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? def : v.trim();
    }

    private static int parseIntOrDefault(String key, int def) {
        String v = System.getenv(key);
        if (v == null || v.isBlank()) {
            return def;
        }
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException ex) {
            return def;
        }
    }

    // Expose for logging if you need it during debugging (not used by repo code)
    @Override
    public String toString() {
        return String.format("Db{host='%s', port=%d, name='%s', user='%s'}", host, port, name, user);
    }
}
