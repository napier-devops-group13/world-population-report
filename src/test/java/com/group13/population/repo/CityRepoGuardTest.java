package com.group13.population.repo;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Guard test to make sure the MySQL database is up for City repo tests.
 *
 * If the database is not reachable on the configured host/port, this test will
 * be skipped (so local "mvn test" can run without Docker), but in CI where the
 * DB is running the test will pass.
 */
public class CityRepoGuardTest {

    @Test
    void databaseIsAvailableForCityRepo() {
        String host = getHost();
        int port = getPort();

        boolean reachable = isPortOpen(host, port, 2000);
        Assumptions.assumeTrue(
            reachable,
            () -> "Failed to connect to database for CityRepoGuardTest at "
                + host + ":" + port
        );
        // If reachable, the assumption passes and the test succeeds.
    }

    private String getHost() {
        String sys = System.getProperty("db.host");
        if (sys != null && !sys.isBlank()) {
            return sys;
        }
        String env = System.getenv("DB_HOST");
        if (env != null && !env.isBlank()) {
            return env;
        }
        return "localhost";
    }

    private int getPort() {
        String sys = System.getProperty("db.port");
        if (sys != null && !sys.isBlank()) {
            return Integer.parseInt(sys);
        }
        String env = System.getenv("DB_PORT");
        if (env != null && !env.isBlank()) {
            return Integer.parseInt(env);
        }
        return 43306; // default test DB port
    }

    private boolean isPortOpen(String host, int port, int timeoutMs) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }
}
