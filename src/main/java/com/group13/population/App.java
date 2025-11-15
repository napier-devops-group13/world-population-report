package com.group13.population;

import com.group13.population.repo.WorldRepo;
import com.group13.population.service.CountryService;
import com.group13.population.web.CountryRoutes;
import io.javalin.Javalin;

import java.io.InputStream;
import java.util.Properties;

/**
 * Application entry point for the World Population reporting API.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Load configuration (port) from environment and/or {@code application.properties}.</li>
 *   <li>Wire together repository → service → HTTP routes.</li>
 *   <li>Start and stop the Javalin web server.</li>
 * </ul>
 */
public final class App {

    /** Default HTTP port used when no env/config value is provided. */
    private static final int DEFAULT_PORT = 7070;

    /** Utility class – no instances. */
    private App() { }

    /**
     * Process entry point used in normal runs.
     * <p>
     * Delegates to {@link #start(int)} with {@code -1} so that
     * the port is resolved from env/config/defaults.
     */
    public static void main(String[] args) {
        // Normal run – use config/env/default (7070)
        start(-1);
    }

    /**
     * Convenience overload for callers that do not care about the port.
     * <p>
     * Equivalent to {@code start(-1)}.
     */
    public static Javalin start() {
        return start(-1);
    }

    /**
     * Start the application and return the running {@link Javalin} instance.
     *
     * <p>Port selection rules:</p>
     * <ul>
     *   <li>{@code overridePort > 0}  → bind exactly to that port</li>
     *   <li>{@code overridePort == 0} → ask OS for any free port (ideal for tests)</li>
     *   <li>{@code overridePort < 0}  → read port from env / application.properties / default</li>
     * </ul>
     *
     * @param overridePort special port handling as described above
     * @return the started Javalin server
     */
    public static Javalin start(int overridePort) {
        Properties props = loadProps();

        int configuredPort =
            getIntEnv("PORT", getIntProp(props, "app.port", DEFAULT_PORT));

        // Decide which port to use based on overridePort
        int port;
        if (overridePort > 0) {
            port = overridePort;
        } else if (overridePort == 0) {
            // Ask OS for any free port – avoids “port already in use” in tests
            port = 0;
        } else {
            port = configuredPort;
        }

        // Application wiring: repository → service → routes
        WorldRepo repo = new WorldRepo();
        CountryService service = new CountryService(repo);
        CountryRoutes routes = new CountryRoutes(service);

        // Create Javalin without the banner to keep logs clean
        Javalin app = Javalin.create(cfg -> cfg.showJavalinBanner = false);

        // Register all HTTP endpoints and a simple health check
        routes.register(app);
        app.get("/health", ctx -> ctx.result("OK"));

        app.start(port);

        // Ensure the server shuts down cleanly when the JVM exits
        Runtime.getRuntime().addShutdownHook(new Thread(app::stop));

        return app;
    }

    // ---------- helpers ----------

    /**
     * Load {@code application.properties} from the classpath.
     * Falls back to an empty {@link Properties} object when the file
     * is missing or cannot be read.
     */
    private static Properties loadProps() {
        Properties props = new Properties();
        try (InputStream in = App.class.getClassLoader()
            .getResourceAsStream("application.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception ignored) {
            // On any error we simply fall back to defaults.
        }
        return props;
    }

    /**
     * Read an integer value from an environment variable, returning a
     * default when unset or not a valid integer.
     */
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

    /**
     * Read an integer value from {@link Properties}, returning a default
     * when the key is missing or not a valid integer.
     */
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
