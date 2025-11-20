package com.group13.population.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Thin wrapper around a JDBC connection to the {@code world} database.
 *
 * <p>Configuration is provided via a {@code host:port} "location" string
 * and environment variables:</p>
 *
 * <ul>
 *   <li>{@code DB_NAME} – database name (defaults to {@code world})</li>
 *   <li>{@code DB_USER} – username (defaults to {@code app})</li>
 *   <li>{@code DB_PASS} – password (defaults to {@code app})</li>
 * </ul>
 *
 * <p>These defaults match docker-compose.yml:</p>
 *
 * <pre>
 *   MYSQL_DATABASE: world
 *   MYSQL_USER:     app
 *   MYSQL_PASSWORD: app
 * </pre>
 */
public class Db {

    /** Reused JDBC connection (if established). */
    private Connection connection;

    /** Last host:port used, so we can reconnect lazily. */
    private String lastLocation;

    /**
     * Attempt to connect to the MySQL database at {@code location} within
     * {@code delayMillis}.
     *
     * @param location    {@code "host:port"}, e.g. {@code "localhost:43306"} or {@code "db:3306"}
     * @param delayMillis maximum time to keep retrying before giving up
     * @return {@code true} if a connection was established, {@code false} otherwise
     */
    public boolean connect(String location, int delayMillis) {
        this.lastLocation = location;

        String dbName = getenvOrDefault("DB_NAME", "world");
        String user = getenvOrDefault("DB_USER", "app");
        String pass = getenvOrDefault("DB_PASS", "app");

        String url = "jdbc:mysql://" + location + "/" + dbName
            + "?useSSL=false"
            + "&allowPublicKeyRetrieval=true"
            + "&serverTimezone=UTC";

        long deadline = System.currentTimeMillis() + delayMillis;

        while (true) {
            try {
                // Ensure driver is loaded (Lab 7 style).
                Class.forName("com.mysql.cj.jdbc.Driver");

                connection = DriverManager.getConnection(url, user, pass);
                return true;
            } catch (SQLException | ClassNotFoundException ex) {
                // Out of time? give up.
                if (System.currentTimeMillis() >= deadline) {
                    connection = null;
                    return false;
                }
                // Otherwise, wait a bit then retry.
                try {
                    Thread.sleep(1_000L);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    connection = null;
                    return false;
                }
            }
        }
    }

    /**
     * Get the active JDBC connection. If the connection has not been created
     * yet but we know the last location, this method will try to reconnect
     * once using a short timeout.
     *
     * @return an open {@link Connection}
     * @throws SQLException if no connection can be obtained
     */
    public Connection getConnection() throws SQLException {
        tryReconnectIfNeeded();
        if (connection == null || connection.isClosed()) {
            throw new SQLException("Database not connected");
        }
        return connection;
    }

    /**
     * Close the current connection if open.
     */
    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
                // ignore close failures
            } finally {
                connection = null;
                // keep lastLocation so we could reconnect later if needed
            }
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void tryReconnectIfNeeded() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    return; // Already open
                }
            } catch (SQLException ignored) {
                // fall through and try to reconnect
            }
        }

        if (lastLocation != null) {
            // Best-effort quick reconnect (5 seconds)
            connect(lastLocation, 5_000);
        }
    }

    private static String getenvOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }
}
