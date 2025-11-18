package com.group13.population;

import com.group13.population.repo.CityWorldRepo;
import com.group13.population.service.CityService;
import com.group13.population.web.CityRoutes;
import io.javalin.Javalin;

import java.io.InputStream;
import java.util.Properties;

/**
 * Application entry point for the World Population reporting API.
 *
 * Supports:
 *  - R07–R16  City reports  (/api/cities/...)
 */
public final class App {

    /** Default HTTP port used when no env/config value is provided. */
    private static final int DEFAULT_PORT = 7070;

    private App() {
        // utility class – no instances
    }

    /**
     * Normal process entry point.
     * Uses env/config/defaults to choose the port.
     */
    public static void main(String[] args) {
        start(-1);
    }

    /** Convenience overload for callers that don’t care about the port. */
    public static Javalin start() {
        return start(-1);
    }

    /**
     * Start the application and return the running Javalin instance.
     *
     * Port selection rules:
     *  - overridePort > 0  → bind exactly to that port
     *  - overridePort == 0 → “any free port” (useful for tests)
     *  - overridePort < 0  → use env / application.properties / default
     */
    public static Javalin start(int overridePort) {
        Properties props = loadProps();

        int configuredPort =
            getIntEnv("PORT", getIntProp(props, "app.port", DEFAULT_PORT));

        int port;
        if (overridePort > 0) {
            port = overridePort;
        } else if (overridePort == 0) {
            port = 0;        // ask OS for a free port
        } else {
            port = configuredPort;
        }

        // ================== CITY REPORTS (R07–R16) ===================
        CityWorldRepo cityRepo = new CityWorldRepo();   // uses Db.connect(...) internally
        CityService cityService = new CityService(cityRepo);
        CityRoutes cityRoutes = new CityRoutes(cityService);

        // ================= Javalin wiring ============================
        Javalin app = Javalin.create(cfg -> cfg.showJavalinBanner = false);

        // Register HTTP endpoints
        cityRoutes.register(app);      // /api/cities/...
        app.get("/health", ctx -> ctx.result("OK"));

        app.start(port);

        // Clean shutdown when JVM exits
        Runtime.getRuntime().addShutdownHook(new Thread(app::stop));

        return app;
    }

    // ---------- helpers ----------

    /** Load application.properties from the classpath if present. */
    private static Properties loadProps() {
        Properties props = new Properties();
        try (InputStream in = App.class.getClassLoader()
            .getResourceAsStream("application.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception ignored) {
            // fall back to defaults
        }
        return props;
    }

    /** Read an int from an environment variable, with a safe default. */
    private static int getIntEnv(String name, int defaultValue) {
        String raw = System.getenv(name);
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    /** Read an int from Properties, with a safe default. */
    private static int getIntProp(Properties props, String key, int defaultValue) {
        String raw = props.getProperty(key);
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}
