package com.group13.population.web;

import com.group13.population.App;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import okhttp3.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple smoke tests for {@link App}.
 *
 * <p>These tests give confidence that the application can start using
 * {@link App#createApp()} and that the shared health-check endpoint is
 * correctly configured.</p>
 *
 * <p>They deliberately only call the {@code /health} endpoint so they do
 * not require a running database, keeping the smoke tests fast and stable.</p>
 */
public class AppSmokeTest {

    @Test
    @DisplayName("Smoke – App.createApp starts a server with /health returning OK")
    void healthEndpointIsReachable() throws Exception {
        // Build an un-started app instance; JavalinTest will start and stop it.
        JavalinTest.test(App.createApp(), (server, client) -> {
            try (Response res = client.get("/health")) {
                assertEquals(200, res.code(), "Expected HTTP 200 from /health");
                assertNotNull(res.body(), "Response body should not be null");
                assertEquals("OK", res.body().string(), "Expected body 'OK' from /health");
            }
        });
    }

    @Test
    @DisplayName("Smoke – unknown path returns 404 (app is running and routing works)")
    void unknownPathReturns404() throws Exception {
        JavalinTest.test(App.createApp(), (server, client) -> {
            try (Response res = client.get("/this-path-does-not-exist")) {
                assertEquals(404, res.code(), "Unknown paths should return 404");
            }
        });
    }

    @Test
    @DisplayName("Smoke – App.start starts a real server instance which can be stopped")
    void startStartsAndStopsServer() {
        Javalin app = null;
        try {
            // This exercises App.start(), including:
            //  - loadProps()
            //  - getIntEnv("PORT", ...)
            //  - getIntProp(...)
            //  - createApp()
            app = App.start();

            assertNotNull(app, "App.start should return a non-null Javalin instance");
        } finally {
            // Ensure we always stop the server so the port is freed for other tests.
            if (app != null) {
                app.stop();
            }
        }
    }
}
