package com.group13.population;

import com.group13.population.db.Db;
import com.group13.population.repo.WorldRepo;
import com.group13.population.service.CountryService;
import com.group13.population.web.CountryRoutes;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

public final class App {

    private App() {
    }

    public static void main(String[] args) {
        start();
    }

    /**
     * Start the HTTP server using config from app.properties + environment.
     */
    public static Javalin start() {
        Properties props = loadProps();
        int port = getIntEnv("PORT", getIntProp(props, "app.port", 7070));

        Javalin app = createApp(props);
        app.start(port);

        // Graceful shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(app::stop));
        return app;
    }

    /**
     * Factory used by tests and by {@link #start()}.
     * Builds the Javalin app but does NOT call start().
     */
    public static Javalin createApp() {
        Properties props = loadProps();
        return createApp(props);
    }

    private static Javalin createApp(Properties props) {
        // 1. Connect DB using env + properties
        Db db = new Db();
        connectDbFromConfig(db, props);

        // 2. Wire repo → service → routes
        WorldRepo repo = new WorldRepo(db);
        CountryService service = new CountryService(repo);
        CountryRoutes routes = new CountryRoutes(service);

        // 3. Build Javalin instance (not started)
        Javalin app = Javalin.create((JavalinConfig cfg) -> {
            cfg.showJavalinBanner = false;
        });

        routes.register(app);
        app.get("/health", ctx -> ctx.result("OK"));

        return app;
    }

    /**
     * Connect the Db using environment variables if present,
     * otherwise fall back to app.properties.
     *
     * Expected properties (with defaults):
     *   db.host=db
     *   db.port=3306
     *   db.startupDelay=0
     */
    static void connectDbFromConfig(Db db, Properties props) {
        Objects.requireNonNull(db, "db");
        Objects.requireNonNull(props, "props");

        String host = System.getenv("DB_HOST");
        if (host == null || host.isBlank()) {
            host = props.getProperty("db.host", "localhost");
        }

        int port = getIntEnv("DB_PORT", getIntProp(props, "db.port", 3306));
        int delay = getIntEnv("DB_STARTUP_DELAY",
            getIntProp(props, "db.startupDelay", 0));

        String location = host + ":" + port;
        System.out.println("DEBUG: App connecting to DB at "
            + location + " with startup delay " + delay + "ms");

        try {
            db.connect(location, delay);
        } catch (Exception ex) {
            // If this fails, WorldRepo will just return empty lists,
            // but at least we see the reason in logs.
            System.err.println("ERROR: DB connection failed: " + ex.getMessage());
        }
    }

    // ---------------------------------------------------------------------
    // Helper methods used by AppHelpersTest
    // ---------------------------------------------------------------------

    /** Load application properties from app.properties on the classpath. */
    public static Properties loadProps() {
        Properties props = new Properties();
        try (InputStream in = App.class.getClassLoader()
            .getResourceAsStream("app.properties")) {

            if (in != null) {
                props.load(in);
            } else {
                System.err.println("WARNING: app.properties not found on classpath");
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load app.properties", ex);
        }
        return props;
    }

    /** Read an integer property with default + error handling. */
    public static int getIntProp(Properties props, String key, int defaultValue) {
        Objects.requireNonNull(props, "props");
        String raw = props.getProperty(key);
        if (raw == null || raw.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    /** Read an integer environment variable with default + error handling. */
    public static int getIntEnv(String name, int defaultValue) {
        String raw = System.getenv(name);
        if (raw == null || raw.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}
