package com.group13.population.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * JDBC helper for the seeded "world" database.
 *
 * Environment variables (with sensible defaults for local compose):
 *  - DB_HOST (default 127.0.0.1)
 *  - DB_PORT (default 43306)  // your compose publishes 43306->3306
 *  - DB_NAME (default world)
 *  - DB_USER (default app)
 *  - DB_PASS (default app)
 */
public final class Db {

    // Default max wait used by connect(); App.create() may override.
    private static final long DEFAULT_MAX_WAIT_MS = 120_000L; // 2 minutes

    private final String host;
    private final int port;
    private final String name;
    private final String user;
    private final String pass;
    private final String jdbcUrl;

    public Db() {
        this.host = getenvOrDefault("DB_HOST", "127.0.0.1");
        this.port = parseIntOrDefault("DB_PORT", 43306);
        this.name = getenvOrDefault("DB_NAME", "world");
        this.user = getenvOrDefault("DB_USER", "app");
        this.pass = getenvOrDefault("DB_PASS", "app");

        this.jdbcUrl = String.format(
            "jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
            host, port, name
        );
    }

    /** Returns a live JDBC connection. Blocks briefly until DB is ready. */
    public Connection connect() throws SQLException {
        awaitReady(DEFAULT_MAX_WAIT_MS, 300L);
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
                last = e; // keep last seen to report if we time out
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

    private static String getenvOrDefault(final String key, final String def) {
        final String v = System.getenv(key);
        if (v == null || v.isBlank()) {
            return def;
        }
        return v.trim();
    }

    private static int parseIntOrDefault(final String key, final int def) {
        final String v = System.getenv(key);
        if (v == null || v.isBlank()) {
            return def;
        }
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException ex) {
            return def;
        }
    }

    @Override
    public String toString() {
        return String.format("Db{host='%s', port=%d, name='%s', user='%s'}", host, port, name, user);
    }
}
