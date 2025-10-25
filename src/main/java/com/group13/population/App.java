package com.group13.population;

import com.group13.population.repo.WorldRepo;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;

import java.sql.SQLException;
import java.util.Map;

/**
 * Application entry point for the World Population Report service.
 * Exposes basic health endpoints and the first requirement (R26).
 */
public final class App {

    private App() {
        // Utility class â€“ no instances.
    }

    /**
     * Reads an integer from the environment with a safe default.
     *
     * @param key          the environment variable name (e.g., "PORT")
     * @param defaultValue the value to return if the env var is missing or invalid
     * @return the parsed integer value
     */
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

    /**
     * Starts the HTTP server.
     *
     * @param args standard command-line arguments (unused)
     */
    public static void main(final String[] args) {
        final int port = envInt("PORT", 7000);

        final Javalin app = Javalin.create(config -> {
                // Request log: HttpStatus is not an int; print with %s to avoid format errors.
                config.requestLogger.http((ctx, ms) ->
                    System.out.printf("%s %s -> %s (%d ms)%n",
                        ctx.method(), ctx.path(), ctx.status(), ms));
            })
            // Global safety net so unexpected exceptions become JSON 500s (final useful in CI/demo)
            .exception(Exception.class, (e, ctx) -> {
                e.printStackTrace();
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(Map.of("error", "internal_error", "message", e.getMessage()));
            })
            .start(port);

        // -------- Health probes --------
        app.get("/", ctx -> ctx.result("World Population Report API â€” try /health or /population/world"));

        // Liveness: server is up
        app.get("/health", ctx -> ctx.result("OK"));

        // Readiness: touch DB; 503 if DB not reachable yet
        app.get("/ready", ctx -> {
            try {
                new WorldRepo().worldPopulation(); // simple DB touch
                ctx.result("READY");
            } catch (final SQLException ex) {
                ctx.status(HttpStatus.SERVICE_UNAVAILABLE).result("DB_NOT_READY");
            }
        });

        // -------- R26: Population of the world --------
        app.get("/population/world", ctx -> {
            try {
                final long total = new WorldRepo().worldPopulation();
                ctx.json(Map.of("scope", "world", "population", total));
            } catch (final SQLException ex) {
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(Map.of("error", "db_error", "message", ex.getMessage()));
            }
        });
    }
}

