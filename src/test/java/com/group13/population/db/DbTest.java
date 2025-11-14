package com.group13.population.db;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DbTest {

    @Test
    void buildJdbcUrlFormatsCorrectly() {
        String url = Db.buildJdbcUrl("example.com", 3306, "world");

        assertEquals(
            "jdbc:mysql://example.com:3306/world?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
            url
        );
    }

    @Test
    void connectToInvalidHostThrowsSQLException() {
        // Use a port that is almost certainly closed; we EXPECT failure.
        assertThrows(SQLException.class, () ->
            Db.connect("127.0.0.1", 65530, "world", "app", "app")
        );
    }
}
