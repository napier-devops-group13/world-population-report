package com.group13.population.db;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DbTest {

    @Test
    void buildJdbcUrlFormatsCorrectly() {
        String url = Db.buildJdbcUrl("example.com", 3306, "world");

        assertEquals(
            "jdbc:mysql://example.com:3306/world?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
            url
        );
    }
}
