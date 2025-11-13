package com.group13.population.web;

import com.group13.population.App;
import io.javalin.Javalin;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Very small smoke test: can the app start and stop?
 * We don’t hit any real DB here (WorldRepo will only query on demand).
 */
class AppSmokeTest {

    @Test
    void appCanStartAndStop() {
        Javalin app = App.start();   // this should run all the main logic in App.start()
        assertNotNull(app, "App.start() should return a Javalin instance");
        app.stop();
    }
}
