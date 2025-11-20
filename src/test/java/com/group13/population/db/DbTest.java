package com.group13.population.db;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Db}.
 *
 * These tests focus on the basic behaviour of the Db wrapper
 * without requiring a real MySQL instance. The "happy path"
 * (real connection, queries, etc.) is covered by {@link DbIT}.
 */
class DbTest {

    @Test
    @DisplayName("getConnection throws SQLException when not connected")
    void getConnectionThrowsWhenNotConnected() {
        Db db = new Db();

        assertThrows(SQLException.class,
            db::getConnection,
            "Calling getConnection() before connect() should throw SQLException");
    }

    @Test
    @DisplayName("disconnect is safe when no connection was ever opened")
    void disconnectIsSafeWhenNeverConnected() {
        Db db = new Db();

        // Should not throw, even though we never called connect()
        assertDoesNotThrow(
            db::disconnect,
            "disconnect() should be safe when no connection is open"
        );
    }

    @Test
    @DisplayName("connect returns false for an obviously invalid host/port")
    void connectReturnsFalseForInvalidLocation() {
        Db db = new Db();

        // Port 1 on localhost is almost certainly not a MySQL server.
        boolean connected = db.connect("127.0.0.1:1", 500);

        assertFalse(
            connected,
            "connect() to an invalid location should return false"
        );
    }
}
