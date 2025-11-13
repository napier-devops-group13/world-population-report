package com.group13.population.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Minimal JDBC helper for MySQL.
 * Reads DB_ env vars in the same way as App/main + application.properties.
 */
public final class Db {
    private Db() {
        // utility
    }

    public static Connection connect(
        final String host,
        final int port,
        final String database,
        final String user,
        final String pass
    ) throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ignored) {
            // driver is loaded by SPI in newer JDKs, but keep this for safety
        }

        String url = "jdbc:mysql://"
            + host + ":" + port + "/" + database
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        return DriverManager.getConnection(url, user, pass);
    }
}
