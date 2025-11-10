package com.group13.population;

import com.group13.population.db.Db;
import com.group13.population.repo.WorldRepo;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.Map;
import java.util.function.Function;

/**
 * App bootstrap + routes for Country reports (R01–R06).
 */
public final class App {

    private static final int DEFAULT_PORT = 7070;

    private App() {
        // empty ctor (space inside braces)
    }

    public static void main(final String[] args) {
        int port = readPort();
        Javalin app = create();
        app.start(port);
        System.out.println("Listening on http://localhost:" + port);
    }

    /**
     * Build a configured Javalin app with all routes registered.
     */
    public static Javalin create() {
        // 1) Ensure DB is ready before serving
        Db db = new Db();
        db.awaitReady(30_000L, 500L);

        // 2) Repo
        WorldRepo repo = new WorldRepo(db);

        // 3) App + common error mapping + routes
        Javalin app = Javalin.create(cfg -> {
            cfg.plugins.enableCors(cors -> cors.add(it -> it.anyHost()));
            cfg.showJavalinBanner = false;
            cfg.http.defaultContentType = "application/json";
        });

        // Always JSON for 400 (bad input)
        app.exception(IllegalArgumentException.class, (e, ctx) ->
            ctx.status(HttpStatus.BAD_REQUEST)
                .json(Map.of("error", e.getMessage()))
        );

        // Guard rail for unexpected errors
        app.exception(Exception.class, (e, ctx) ->
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .json(Map.of("error", "internal server error"))
        );

        registerRoutes(app, repo, "");
        return app;
    }

    /**
     * Resolve port from APP_PORT (defaults to 7070).
     */
    private static int readPort() {
        String raw = System.getenv("APP_PORT");
        if (raw == null || raw.isBlank()) {
            return DEFAULT_PORT;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException nfe) {
            return DEFAULT_PORT;
        }
    }

    /**
     * Register REST routes for R01–R06.
     *
     * @param app    Javalin app
     * @param repo   Repository providing country data
     * @param prefix Optional URL prefix ("" for normal app, "/api" for tests etc.)
     */
    public static void registerRoutes(final Javalin app,
                                      final WorldRepo repo,
                                      final String prefix) {

        final String p = (prefix == null) ? "" : prefix;

        // Simple health check
        app.get(p + "/health", ctx -> ctx.result("ok"));

        // Helper: ?sort=pop (case-insensitive)
        Function<Context, Boolean> sortByPop = ctx -> {
            String s = ctx.queryParam("sort");
            return s != null && "pop".equalsIgnoreCase(s.trim());
        };

        // Helper: parse positive N (throws 400 with clear JSON)
        Function<Context, Integer> parsePositiveN = ctx -> {
            String raw = ctx.pathParam("n");
            try {
                int n = Integer.parseInt(raw);
                if (n <= 0) {
                    throw new IllegalArgumentException("n must be > 0");
                }
                return n;
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("n must be a positive integer");
            }
        };

        // R01: all countries (world)
        app.get(p + "/countries/world", ctx -> {
            if (sortByPop.apply(ctx)) {
                ctx.json(repo.countriesWorldByPopulation());
            } else {
                ctx.json(repo.countriesWorld());
            }
        });

        // R02: all countries in a continent
        app.get(p + "/countries/continent/{continent}", ctx -> {
            String continent = ctx.pathParam("continent");
            if (sortByPop.apply(ctx)) {
                ctx.json(repo.countriesByContinentByPopulation(continent));
            } else {
                ctx.json(repo.countriesByContinent(continent));
            }
        });

        // R03: all countries in a region
        app.get(p + "/countries/region/{region}", ctx -> {
            String region = ctx.pathParam("region");
            if (sortByPop.apply(ctx)) {
                ctx.json(repo.countriesByRegionByPopulation(region));
            } else {
                ctx.json(repo.countriesByRegion(region));
            }
        });

        // R04: top-N countries (world)
        app.get(p + "/countries/world/top/{n}", ctx -> {
            int n = parsePositiveN.apply(ctx);
            ctx.json(repo.topCountriesWorld(n));
        });

        // R05: top-N countries (continent)
        app.get(p + "/countries/continent/{continent}/top/{n}", ctx -> {
            String continent = ctx.pathParam("continent");
            int n = parsePositiveN.apply(ctx);
            ctx.json(repo.topCountriesByContinent(continent, n));
        });

        // R06: top-N countries (region)
        app.get(p + "/countries/region/{region}/top/{n}", ctx -> {
            String region = ctx.pathParam("region");
            int n = parsePositiveN.apply(ctx);
            ctx.json(repo.topCountriesByRegion(region, n));
        });
    }
}
