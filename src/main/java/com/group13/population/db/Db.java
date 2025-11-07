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
        this.port = parseIntOrDefault("DB_PORT", 43306); // matches docker-compose published port
        this.name = getenvOrDefault("DB_NAME", "world");
        this.user = getenvOrDefault("DB_USER", "app");
        this.pass = getenvOrDefault("DB_PASS", "app");

        // Short connect/socket timeouts so retries are fast; keep SSL off for local dev.
        this.jdbcUrl = String.format(
            "jdbc:mysql://%s:%d/%s"
                + "?useSSL=false"
                + "&allowPublicKeyRetrieval=true"
                + "&useUnicode=true&characterEncoding=utf8"
                + "&serverTimezone=UTC"
                + "&connectTimeout=5000&socketTimeout=5000",
            host, port, name);
    }

    /** Obtain a live JDBC connection. Blocks (with retries) until DB + data are ready. */
    public Connection connect() throws SQLException {
        // Wait until MySQL accepts connections AND the world dataset is present
        awaitReady(DEFAULT_MAX_WAIT_MS, 400L);
        return DriverManager.getConnection(jdbcUrl, user, pass);
    }

    /**
     * Poll until the DB responds and the 'country' table has rows (dataset loaded),
     * or the timeout elapses.
     *
     * @param maxWaitMs     total time to wait
     * @param firstSleepMs  initial sleep between attempts; will back off up to 3s
     */
    public void awaitReady(final long maxWaitMs, final long firstSleepMs) {
        final long deadline = System.currentTimeMillis() + Math.max(maxWaitMs, 30_000L);
        long sleep = Math.max(firstSleepMs, 250L);
        SQLException last = null;

        while (System.currentTimeMillis() < deadline) {
            try (Connection c = DriverManager.getConnection(jdbcUrl, user, pass)) {
                // Ping and ensure seed data is available (avoids racing the import in CI)
                try (PreparedStatement ping = c.prepareStatement("SELECT 1");
                     ResultSet rsPing = ping.executeQuery()) {
                    if (!rsPing.next()) throw new SQLException("Ping failed (no row).");
                }
                try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM country");
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getLong(1) > 0L) {
                        return; // Ready: schema + data available
                    }
                }
            } catch (SQLException e) {
                last = e; // remember and retry
            }

            try {
                Thread.sleep(sleep);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Waiting for DB was interrupted.", ie);
            }
            // Exponential backoff with cap
            sleep = Math.min((long) (sleep * 1.5), 3000L);
        }

        throw new IllegalStateException("Database not ready after wait.", last);
    }

    // ---------------- helpers ----------------

    private static String getenvOrDefault(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? def : v.trim();
    }

    private static int parseIntOrDefault(String key, int def) {
        String v = System.getenv(key);
        if (v == null || v.isBlank()) return def;
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
