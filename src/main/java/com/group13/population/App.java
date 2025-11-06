package com.group13.population;

import com.group13.population.db.Db;
import com.group13.population.repo.WorldRepo;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import java.util.Map;

/** Application bootstrap: wires Javalin, repo, and routes for R01–R06 (Country reports). */
public final class App {

    private App() { }

    /** Build a configured Javalin app with all routes registered. */
    public static Javalin create() {
        // 1) Ensure DB is reachable before serving
        Db db = new Db();
        db.awaitReady(30_000L, 500L);

        // 2) Repo
        WorldRepo repo = new WorldRepo(db);

        // 3) App + common error mapping
        Javalin app = Javalin.create((JavalinConfig cfg) -> { /* defaults ok */ });

        // Return 400 as JSON for any thrown IllegalArgumentException
        app.exception(IllegalArgumentException.class, (e, ctx) -> {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", e.getMessage()));
        });

        // Return 500 JSON for other unexpected errors
        app.exception(Exception.class, (e, ctx) -> {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .json(Map.of("error", "internal server error"));
        });

        registerRoutes(app, repo, "");
        return app;
    }

    /** Start server (used by shaded JAR). */
    public static void main(String[] args) {
        Javalin app = create();
        app.start(7070);
        System.out.println("Listening on http://localhost:7070");
    }

    /** Register REST routes (R01–R06). */
    public static void registerRoutes(final Javalin app, final WorldRepo repo, final String prefix) {
        final String p = prefix == null ? "" : prefix;

        // Helper: check ?sort=pop (case-insensitive)
        java.util.function.Function<Context, Boolean> sortByPop =
            ctx -> {
                String s = ctx.queryParam("sort");
                return s != null && "pop".equalsIgnoreCase(s.trim());
            };

        // Helper: parse positive integer for top-N endpoints
        java.util.function.Function<Context, Integer> parsePositiveN =
            ctx -> {
                String raw = ctx.pathParam("n");
                try {
                    int n = Integer.parseInt(raw);
                    if (n <= 0) throw new IllegalArgumentException("n must be > 0");
                    return n;
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException("n must be a positive integer");
                }
            };

        // R01: all countries (world)
        app.get(p + "/countries/world", ctx -> {
            if (sortByPop.apply(ctx)) ctx.json(repo.countriesWorldByPopulation());
            else ctx.json(repo.countriesWorld());
        });

        // R02: all countries in a continent
        app.get(p + "/countries/continent/{continent}", ctx -> {
            String continent = ctx.pathParam("continent");
            if (sortByPop.apply(ctx)) ctx.json(repo.countriesByContinentByPopulation(continent));
            else ctx.json(repo.countriesByContinent(continent));
        });

        // R03: all countries in a region
        app.get(p + "/countries/region/{region}", ctx -> {
            String region = ctx.pathParam("region");
            if (sortByPop.apply(ctx)) ctx.json(repo.countriesByRegionByPopulation(region));
            else ctx.json(repo.countriesByRegion(region));
        });

        // R04: top-N countries (world)
        app.get(p + "/countries/world/top/{n}", ctx -> {
            int n = parsePositiveN.apply(ctx);
            ctx.json(repo.topCountriesWorld(n));
        });

        // R05: top-N countries in continent
        app.get(p + "/countries/continent/{continent}/top/{n}", ctx -> {
            String continent = ctx.pathParam("continent");
            int n = parsePositiveN.apply(ctx);
            ctx.json(repo.topCountriesByContinent(continent, n));
        });

        // R06: top-N countries in region
        app.get(p + "/countries/region/{region}/top/{n}", ctx -> {
            String region = ctx.pathParam("region");
            int n = parsePositiveN.apply(ctx);
            ctx.json(repo.topCountriesByRegion(region, n));
        });
    }
}
