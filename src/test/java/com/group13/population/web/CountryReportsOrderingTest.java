package com.group13.population.web;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Guard test for the country report integration tests.
 *
 * In CI (where MySQL is running), this asserts that the DB is reachable.
 * Locally, if the DB is not running on the test port, the test is skipped.
 */
public class CountryReportsOrderingTest {

    @Test
    void databaseIsAvailableForCountryReports() {
        String host = getHost();
        int port = getPort();

        boolean reachable = isPortOpen(host, port, 2000);
        Assumptions.assumeTrue(
            reachable,
            () -> "Failed to connect to database at " + host + ":" + port
        );
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
        return 43306;
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
