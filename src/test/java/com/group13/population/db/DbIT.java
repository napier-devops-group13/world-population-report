package com.group13.population.db;

import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertFalse;

class DbIT {

    @Test
    void connectWithValidConfig() throws Exception {
        // This must match your docker-compose host + port
        Connection c = Db.connect("localhost", 43306, "world", "app", "app");
        assertFalse(c.isClosed());
        c.close();
    }
}
