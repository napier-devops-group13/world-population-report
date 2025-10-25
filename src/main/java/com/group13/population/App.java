package com.group13.population;

import io.javalin.Javalin;
import io.javalin.http.HttpStatus;

/**
 * Entry point for the World Population Report API.
 */
public final class App {

    private App() {
        // utility class: no instances
    }

    public static void main(final String[] args) {
        final int port = envInt("PORT", 7000);

        // Javalin 5 – simplest form (no special config needed)
        final Javalin app = Javalin.create();

        // Basic info (helpful sanity check)
        app.get("/", ctx -> ctx.status(HttpStatus.OK).result("World Population Report API"));

        // Health/ready endpoints (must return HTTP 200 for your PowerShell checks)
        app.get("/ready",  ctx -> ctx.contentType("text/plain").status(200).result("READY"));
        app.get("/health", ctx -> ctx.contentType("text/plain").status(200).result("OK"));


        // Example business endpoint (wire up when your repo/service is ready)
        // app.get("/population/world", ctx -> {
        //   long pop = new WorldRepo().worldPopulation();
        //   ctx.json(java.util.Map.of("population", pop));
        // });

        // Graceful shutdown on JVM exit
        Runtime.getRuntime().addShutdownHook(new Thread(app::stop, "javalin-shutdown"));

        app.start(port);
    }

    /** Reads an integer from the environment with a safe default. */
    private static int envInt(final String key, final int defaultValue) {
        try {
            final String raw = System.getenv(key);
            if (raw == null || raw.isBlank()) {
                return defaultValue;
            }
            return Integer.parseInt(raw.trim());
        } catch (final Exception ex) {
            return defaultValue;
        }
    }
}
