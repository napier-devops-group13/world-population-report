package com.group13.population.web;

import com.group13.population.App;
import io.javalin.Javalin;
import org.junit.jupiter.api.Test;

/**
 * Smoke test to ensure the Javalin application
 * can start and stop without throwing exceptions.
 */
class AppSmokeTest {

    /**
     * Start the app on an ephemeral port (0 = OS-chosen free port)
     * to avoid "port already in use" clashes during tests, then stop it.
     */
    @Test
    void appCanStartAndStop() {
        // 0 -> let the OS pick any free port instead of the normal 7070
        Javalin app = App.start(0);

        // If we reach here without an exception, startup succeeded
        app.stop();
    }
}
