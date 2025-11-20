package com.group13.population.web;

import com.group13.population.App;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import okhttp3.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Configuration and wiring tests for {@link App}.
 */
public class AppConfigTest {

    @Test
    @DisplayName("App.createApp exposes /health endpoint returning OK")
    void healthEndpointIsConfigured() throws Exception {
        // Build an un-started Javalin app instance for the test.
        JavalinTest.test(App.createApp(), (server, client) -> {
            try (Response res = client.get("/health")) {
                assertEquals(200, res.code(), "Expected HTTP 200 from /health");
                assertNotNull(res.body(), "Response body should not be null");
                assertEquals("OK", res.body().string(), "Expected body 'OK' from /health");
            }
        });
    }

    @Test
    @DisplayName("App.createApp can be called multiple times without conflict")
    void createAppIsReusable() {
        Javalin app1 = App.createApp();
        Javalin app2 = App.createApp();

        assertNotNull(app1, "First app instance should not be null");
        assertNotNull(app2, "Second app instance should not be null");
        assertNotSame(app1, app2, "Each call to createApp should produce a new instance");

        // Safe even if never started – ensures cleanup if they are.
        app1.stop();
        app2.stop();
    }
}
