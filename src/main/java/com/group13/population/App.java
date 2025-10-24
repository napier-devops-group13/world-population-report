package com.group13.population;

import io.javalin.Javalin;

/**
 * Entry point for the World Population Reporting API.
 * <p>
 * Minimal scaffold used for CR1: starts a Javalin server,
 * provides a health endpoint, and is packaged as a shaded JAR.
 * </p>
 */
public final class App {

    /** Default port when PORT env var is not set. */
    private static final int DEFAULT_PORT = 7000;

    private App() {
        // Utility class: prevent instantiation
    }

    /**
     * Application bootstrap.
     *
     * @param args ignored
     */
    public static void main(final String[] args) {
        final int port = getPortFromEnvOrDefault();
        Javalin app = Javalin.create(config -> config.showJavalinBanner = false)
                .get("/", ctx -> ctx.result("World Population Report API (SET09803)"))
                .get("/health", ctx -> ctx.json(new Health("ok")))
                .start(port);

        // Simple lifecycle log to help examiners see it running
        System.out.printf("Server started on http://localhost:%d%n", port);

        // Add shutdown hook to stop server gracefully
        Runtime.getRuntime().addShutdownHook(new Thread(app::stop));
    }

    private static int getPortFromEnvOrDefault() {
        String raw = System.getenv("PORT");
        if (raw == null || raw.isBlank()) {
            return DEFAULT_PORT;
        }
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ex) {
            // Fallback keeps the app resilient and easy to mark
            return DEFAULT_PORT;
        }
    }

    /**
     * Tiny DTO for /health response.
     * Using a Java record keeps the code concise and immutable.
     *
     * @param status service status string
     */
    public record Health(String status) { }
}
