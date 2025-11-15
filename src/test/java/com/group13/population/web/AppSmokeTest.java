package com.group13.population.web;

import com.group13.population.App;
import io.javalin.Javalin;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Smoke tests for the top-level App bootstrap.
 * These tests exercise the main port-selection branches in App.start(...).
 */
class AppSmokeTest {

    /**
     * Basic “does it start at all?” smoke test.
     * Uses port 0 so the OS allocates any free port – avoids clashes in CI.
     */
    @Test
    void appCanStartAndStopOnRandomPort() {
        Javalin app = App.start(0);
        try {
            assertTrue(app.port() > 0, "Javalin should be listening on some port");
        } finally {
            app.stop();
        }
    }

    /**
     * When a positive overridePort is supplied, App must bind to that port.
     */
    @Test
    void appHonoursExplicitPortOverride() throws IOException {
        int port = findFreePort();
        Javalin app = App.start(port);
        try {
            assertEquals(port, app.port(), "App.start(int) must honour overridePort");
        } finally {
            app.stop();
        }
    }

    /**
     * A negative overridePort means “use configured/default port”.
     * We only assert that the app starts and listens on a valid port.
     */
    @Test
    void appUsesConfiguredOrDefaultPortWhenOverrideNegative() {
        Javalin app = App.start(-1);
        try {
            assertTrue(app.port() > 0, "App should start on a valid TCP port");
        } finally {
            app.stop();
        }
    }

    /**
     * Ask the OS for a currently free TCP port.
     * The socket is closed immediately so App.start(...) can reuse the port.
     */
    private int findFreePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
