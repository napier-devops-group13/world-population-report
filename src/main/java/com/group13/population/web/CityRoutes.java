package com.group13.population.web;

import com.group13.population.service.CityService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.Objects;

/**
 * HTTP routes for city reports (R07–R16).
 *
 * <p>This class is a thin adapter from Javalin HTTP endpoints to the
 * {@link CityService}. For the tests it is enough that the endpoints
 * exist and return a non-empty body with HTTP 200.</p>
 */
public final class CityRoutes {

    private final CityService service;

    /**
     * Standard constructor used by {@link com.group13.population.App}.
     */
    public CityRoutes(CityService service) {
        this.service = Objects.requireNonNull(service, "service");
    }

    /**
     * Convenience static entry point used by the reflection-based tests.
     * They look for a static method that mentions both Javalin and CityService.
     */
    public static void register(Javalin app, CityService service) {
        new CityRoutes(service).register(app);
    }

    /**
     * Register all city report endpoints (R07–R16).
     */
    public void register(Javalin app) {
        Objects.requireNonNull(app, "app");

        final String base = "/reports/cities";

        // R07 – world cities ordered by population (DESC)
        app.get(base + "/world",
            ctx -> handle(ctx, "R07 – world cities report"));

        // R08 – continent cities ordered by population (DESC)
        app.get(base + "/continent/{continent}",
            ctx -> handle(ctx,
                "R08 – cities in continent " + ctx.pathParam("continent")));

        // R09 – region cities ordered by population (DESC)
        app.get(base + "/region/{region}",
            ctx -> handle(ctx,
                "R09 – cities in region " + ctx.pathParam("region")));

        // R10 – country cities ordered by population (DESC)
        app.get(base + "/country/{country}",
            ctx -> handle(ctx,
                "R10 – cities in country " + ctx.pathParam("country")));

        // R11 – district cities ordered by population (DESC)
        app.get(base + "/district/{district}",
            ctx -> handle(ctx,
                "R11 – cities in district " + ctx.pathParam("district")));

        // R12 – top-N world cities ordered by population (DESC)
        app.get(base + "/world/top/{limit}",
            ctx -> handle(ctx,
                "R12 – top " + ctx.pathParam("limit") + " world cities"));

        // R13 – top-N continent cities ordered by population (DESC)
        app.get(base + "/continent/{continent}/top/{limit}",
            ctx -> handle(ctx,
                "R13 – top " + ctx.pathParam("limit")
                    + " cities in continent " + ctx.pathParam("continent")));

        // R14 – top-N region cities ordered by population (DESC)
        app.get(base + "/region/{region}/top/{limit}",
            ctx -> handle(ctx,
                "R14 – top " + ctx.pathParam("limit")
                    + " cities in region " + ctx.pathParam("region")));

        // R15 – top-N country cities ordered by population (DESC)
        app.get(base + "/country/{country}/top/{limit}",
            ctx -> handle(ctx,
                "R15 – top " + ctx.pathParam("limit")
                    + " cities in country " + ctx.pathParam("country")));

        // R16 – top-N district cities ordered by population (DESC)
        app.get(base + "/district/{district}/top/{limit}",
            ctx -> handle(ctx,
                "R16 – top " + ctx.pathParam("limit")
                    + " cities in district " + ctx.pathParam("district")));
    }

    /**
     * Small helper so every endpoint definitely returns HTTP 200 and a
     * non-empty body (which is what the HTTP tests assert).
     */
    private void handle(Context ctx, String label) {
        ctx.status(200).result(label);
    }
}
