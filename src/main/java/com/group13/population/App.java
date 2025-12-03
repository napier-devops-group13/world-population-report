package com.group13.population;

import com.group13.population.db.Db;
import com.group13.population.repo.CapitalRepo;
import com.group13.population.repo.CityRepo;
import com.group13.population.repo.PopulationRepo;
import com.group13.population.repo.WorldRepo;
import com.group13.population.service.CapitalService;
import com.group13.population.service.CityService;
import com.group13.population.service.CountryService;
import com.group13.population.service.PopulationService;
import com.group13.population.web.CapitalApiRoutes;
import com.group13.population.web.CapitalRoutes;
import com.group13.population.web.CityApiRoutes;
import com.group13.population.web.CityRoutes;
import com.group13.population.web.CountryRoutes;
import com.group13.population.web.PopulationRoutes;
import io.javalin.Javalin;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

/**
 * Main entry point for the World Population Reporting API (R01–R32).
 *
 * <p>This class is responsible only for:</p>
 * <ul>
 *   <li>Loading configuration.</li>
 *   <li>Connecting to the database.</li>
 *   <li>Wiring repositories → services → web routes.</li>
 *   <li>Starting the Javalin HTTP server.</li>
 * </ul>
 *
 * <p>All report logic lives in the repository, service and route classes.</p>
 */
public final class App {

    private App() {
        // Utility class – do not instantiate.
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

    /**
     * Internal factory that wires DB, repositories, services and routes.
     */
    private static Javalin createApp(Properties props) {
        // 1. Connect DB
        Db db = new Db();
        connectDbFromConfig(db, props);

        // 2. Repositories
        WorldRepo worldRepo             = new WorldRepo(db);
        CityRepo cityRepo               = new CityRepo(db);
        CapitalRepo capitalRepo         = new CapitalRepo(db);
        PopulationRepo populationRepo   = new PopulationRepo(db);

        // 3. Services
        CountryService countryService       = new CountryService(worldRepo);
        CityService cityService             = new CityService(cityRepo);
        CapitalService capitalService       = new CapitalService(capitalRepo);
        PopulationService populationService = new PopulationService(populationRepo);

        // 4. Build Javalin instance
        Javalin app = Javalin.create(cfg -> cfg.showJavalinBanner = false);

        // 5. API routes that query the DB directly (CityApiRoutes / CapitalApiRoutes)
        new CityApiRoutes(db).register(app);
        new CapitalApiRoutes(db).register(app);

        // 6. CSV report routes (R01–R32)
        new CountryRoutes(countryService).register(app);          // R01–R06
        CityRoutes.register(app, cityService);                    // R07–R16
        CapitalRoutes.register(app, capitalService);              // R17–R22
        new PopulationRoutes(populationService).register(app);    // R23–R32

        // 7. Simple health check
        app.get("/health", ctx -> ctx.result("OK"));

        return app;
    }

    /**
     * Connect the Db using properties if present, otherwise fall back
     * to environment variables and then to sensible defaults.
     *
     * Expected property keys:
     *   db.host          – DB hostname (e.g. db, localhost)
     *   db.port          – DB port (e.g. 3306)
     *   db.startupDelay  – optional delay in ms before first attempt
     */
    static void connectDbFromConfig(Db db, Properties props) {
        Objects.requireNonNull(db, "db");
        Objects.requireNonNull(props, "props");

        // -------------------------
        // Host: properties → env → default
        // -------------------------
        String host = props.getProperty("db.host");
        if (host == null || host.isBlank()) {
            host = System.getenv("DB_HOST");
        }
        if (host == null || host.isBlank()) {
            host = "127.0.0.1";
        }

        // -------------------------
        // Port: properties → env → default 3306
        // -------------------------
        int port = getIntProp(props, "db.port", -1);
        if (port <= 0) {
            port = getIntEnv("DB_PORT", 3306);
            if (port <= 0) {
                port = 3306;
            }
        }

        // -------------------------
        // Delay: property → env → 0
        // -------------------------
        int delay = getIntProp(
            props,
            "db.startupDelay",
            getIntEnv("DB_STARTUP_DELAY", 0)
        );
        if (delay < 0) {
            delay = 0;
        }

        String location = host + ":" + port;

        System.out.printf(
            "DEBUG: App.connectDbFromConfig -> %s (delay=%dms)%n",
            location, delay
        );

        try {
            db.connect(location, delay);
        } catch (Exception ex) {
            // For the real app we just log; integration tests use Db directly.
            System.err.println("ERROR: DB connection failed: " + ex.getMessage());
        }
    }

    // ---------------------------------------------------------------------
    // Helper methods used by tests
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
