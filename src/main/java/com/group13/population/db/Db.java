package com.group13.population.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Minimal JDBC connection factory that reads configuration from ENV.
 * Works both inside Docker (DB_HOST=db, DB_PORT=3306) and on host (defaults to 43306).
 */
public final class Db {

    private static String env(String k, String d) {
        String v = System.getenv(k);
        return (v == null || v.isBlank()) ? d : v;
    }

    // Defaults for local dev when app runs on the host (compose maps 43306 -> 3306 in the container)
    private static final String HOST = env("DB_HOST", "localhost");
    private static final String PORT = env("DB_PORT", "43306");
    private static final String NAME = env("DB_NAME", "world");
    private static final String USER = env("DB_USER", "root");
    private static final String PASS = env("DB_PASS", "root");

    private static final String URL = String.format(
        "jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
        HOST, PORT, NAME);

    private Db() { }

    /** Opens a new JDBC connection. */
    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
