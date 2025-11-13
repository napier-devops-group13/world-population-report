package com.group13.population;

import com.group13.population.repo.WorldRepo;
import com.group13.population.service.CountryService;
import com.group13.population.web.CountryRoutes;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;

import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

/**
 * Application entrypoint.
 * Wires repo → service → routes and starts a Javalin server.
 */
public final class App {

    /** Default HTTP port if neither env nor properties specify one. */
    public static final int DEFAULT_PORT = 7070;

    private App() {
        // no instances
    }

    public static void main(final String[] args) {
        start();
    }

    /**
     * Create and start the HTTP server.
     *
     * @return the started Javalin instance
     */
    public static Javalin start() {
        Properties props = loadProps();
        int port = getIntEnv("PORT", getIntProp(props, "app.port", DEFAULT_PORT));

        // Wire up dependencies (no DB work here; repo handles it)
        WorldRepo repo = new WorldRepo();
        CountryService service = new CountryService(repo);
        CountryRoutes routes = new CountryRoutes(service);

        // Build the web app
        Javalin app = Javalin.create((JavalinConfig cfg) -> {
            cfg.showJavalinBanner = false;
        });

        // Routes
        routes.register(app);
        app.get("/health", ctx -> ctx.result("OK"));

        // Start and ensure a clean shutdown
        app.start(port);
        Runtime.getRuntime().addShutdownHook(new Thread(app::stop, "javalin-shutdown"));

        return app;
    }

    /* -------------------- helpers -------------------- */

    private static Properties loadProps() {
        Properties p = new Properties();
        try (InputStream in = App.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (in != null) {
                p.load(in);
            }
        } catch (Exception ex) {
            // ignore: fall back to defaults/env
        }
        return p;
    }

    private static int getIntEnv(final String key, final int fallback) {
        String raw = System.getenv(key);
        return parseIntOrDefault(raw, fallback);
    }

    private static int getIntProp(final Properties props, final String key, final int fallback) {
        Objects.requireNonNull(props, "props");
        String raw = props.getProperty(key);
        return parseIntOrDefault(raw, fallback);
    }

    private static int parseIntOrDefault(final String raw, final int fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}
