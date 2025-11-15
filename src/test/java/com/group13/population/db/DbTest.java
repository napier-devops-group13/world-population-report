package com.group13.population.db;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for the {@link Db} helper.
 * Verifies JDBC URL formatting and behaviour when no DB server is available.
 */
class DbTest {

    /**
     * buildJdbcUrl should assemble the correct JDBC URL using
     * host, port, schema and the fixed connection parameters
     * used by this application.
     */
    @Test
    void buildJdbcUrlFormatsCorrectly() {
        String url = Db.buildJdbcUrl("localhost", 3306, "world");

        assertEquals(
            "jdbc:mysql://localhost:3306/world?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
            url
        );
    }

    /**
     * connect() should try to open a real JDBC connection and
     * surface an SQLException when the server is not reachable.
     */
    @Test
    void connectAttemptsConnectionAndFailsWhenNoServer() {
        // Arrange & act & assert in one step: we EXPECT an SQLException
        // because nothing is listening on port 65530.
        assertThrows(SQLException.class, () ->
            Db.connect("127.0.0.1", 65530, "world", "app", "app")
        );
    }
}
