package com.group13.population;

import com.group13.population.repo.WorldRepo;
import com.group13.population.repo.CapitalRepoJdbc;
import com.group13.population.service.CountryService;
import com.group13.population.service.CapitalService;
import com.group13.population.web.CountryRoutes;
import com.group13.population.web.CapitalRoutes;
import io.javalin.Javalin;

import java.io.InputStream;
import java.util.Properties;

/**
 * Application entry point for the World Population reporting API.
 *
 * Supports:
 *  - R01–R06  Country reports  (/api/countries/...)
 *  - R17–R22  Capital reports   (/api/capitals/...)
 */
public final class App {

    /** Default HTTP port used when no env/config value is provided. */
    private static final int DEFAULT_PORT = 7070;

    private App() {
        // utility class
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

        // ================= COUNTRY REPORTS (R01–R06) =================
        WorldRepo countryRepo = new WorldRepo();          // uses Db internally
        CountryService countryService = new CountryService(countryRepo);
        CountryRoutes countryRoutes = new CountryRoutes(countryService);

        // ================= CAPITAL REPORTS (R17–R22) ==================
        CapitalRepoJdbc capitalRepo = new CapitalRepoJdbc();
        CapitalService capitalService = new CapitalService(capitalRepo);
        CapitalRoutes capitalRoutes = new CapitalRoutes(capitalService);

        // ================= Javalin wiring ==============================
        Javalin app = Javalin.create(cfg -> cfg.showJavalinBanner = false);

        // Register ALL HTTP endpoints
        countryRoutes.register(app);   // /api/countries/...
        capitalRoutes.register(app);   // /api/capitals/...
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
